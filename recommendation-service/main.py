import asyncio
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModel
import torch
import torch.nn.functional as F
from sklearn.metrics.pairwise import cosine_similarity
import httpx
from config import settings


app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=settings.CORS_ALLOW_CREDENTIALS,
    allow_methods=settings.CORS_ALLOW_METHODS,
    allow_headers=settings.CORS_ALLOW_HEADERS,
)

class AdResponse(BaseModel):
    adNo: int
    content: str

tokenizer = AutoTokenizer.from_pretrained(settings.MODEL_NAME, trust_remote_code=True)
model = AutoModel.from_pretrained(settings.MODEL_NAME, trust_remote_code=True)


def get_sentence_embeddings(texts):
    if not texts:
        return torch.tensor([])
    inputs = tokenizer(texts, return_tensors="pt", truncation=True, padding=True, max_length=settings.MAX_LENGTH)
    with torch.no_grad():
        outputs = model(**inputs)
    last_hidden_state = outputs.last_hidden_state
    attention_mask = inputs['attention_mask']
    mask = attention_mask.unsqueeze(-1).expand(last_hidden_state.size()).float()
    masked_embeddings = last_hidden_state * mask
    summed = torch.sum(masked_embeddings, dim=1)
    counts = torch.clamp(mask.sum(dim=1), min=1e-9)
    mean_pooled = summed / counts
    normalized_emb = F.normalize(mean_pooled, p=2, dim=1)
    return normalized_emb


async def fetch_pet_media(client, sns_id, token):
    try:
        media_res = await client.get(
            f"{settings.SNS_SERVICE_URL}/api/v1/sns-service/instagram/medias?instagram_id={sns_id}",
            headers={"Authorization": token}
        )
        media_res.raise_for_status()
        medias = media_res.json()
        return medias.get("data") or []
    except httpx.HTTPStatusError as e:
        print(f"HTTP error fetching media for {sns_id}: {e}")
        return []
    except Exception as e:
        print(f"Error fetching media for {sns_id}: {e}")
        return []

@app.post("/hello")
async def hello(ad: AdResponse, request: Request):
    token = request.headers.get("Authorization")

    async with httpx.AsyncClient(timeout=httpx.Timeout(settings.HTTP_TIMEOUT)) as client:
        pet_res = await client.get(
            f"{settings.PET_SERVICE_URL}/api/v1/pet-service/petstars",
            headers={"Authorization": token}
        )
        pets = pet_res.json().get("data", [])

        # 1. 병렬로 미디어 데이터 가져오기
        media_tasks = [fetch_pet_media(client, pet.get("snsId"), token) for pet in pets]
        media_results = await asyncio.gather(*media_tasks)

        pet_media_map = {}
        all_captions = []
        pet_caption_counts = {}

        # 2. 모든 캡션 수집
        for i, (pet, medias) in enumerate(zip(pets, media_results)):
            pet_no = pet.get("petNo")
            valid_captions = [media.get("caption") for media in medias if media.get("caption") and media.get("caption").strip()]
            pet_media_map[pet_no] = valid_captions
            all_captions.extend(valid_captions)
            pet_caption_counts[pet_no] = len(valid_captions)

        # 3. 모든 캡션에 대해 일괄적으로 임베딩 계산 (배치 처리)
        if all_captions:
            all_embeddings = get_sentence_embeddings(all_captions)
        else:
            all_embeddings = torch.tensor([])

        petstars_embeddings = []
        caption_idx = 0
        for pet in pets:
            pet_no = pet.get("petNo")
            num_captions = pet_caption_counts.get(pet_no, 0)
            
            if num_captions > 0:
                pet_embeddings = all_embeddings[caption_idx : caption_idx + num_captions]
                avg_emb = torch.mean(pet_embeddings, dim=0)
                normalized_avg_emb = F.normalize(avg_emb.unsqueeze(0), p=2, dim=1).squeeze()
                caption_idx += num_captions
            else:
                normalized_avg_emb = torch.zeros(model.config.hidden_size)
            
            petstars_embeddings.append({
                "pet_no": pet_no,
                "embedding": normalized_avg_emb
            })
    
    # 광고 임베딩 계산
    ad_emb = get_sentence_embeddings([ad.content]).squeeze()

    similarities = [
        {"pet_no": pet["pet_no"], "similarity": float(cosine_similarity(ad_emb.unsqueeze(0), pet["embedding"].unsqueeze(0))[0][0])}
        for pet in petstars_embeddings
    ]
    
    top = sorted(similarities, key=lambda x: x["similarity"], reverse=True)[:settings.TOP_PETSTARS_COUNT]

    return {
        "ad": ad.adNo,
        "top_petstars": top
    }