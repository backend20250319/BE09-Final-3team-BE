import os
from dotenv import load_dotenv

# .env 파일 로드
load_dotenv()

class Settings:
    # API 서비스 URL들
    PET_SERVICE_URL: str = os.getenv("PET_SERVICE_URL", "http://localhost:8000")
    SNS_SERVICE_URL: str = os.getenv("SNS_SERVICE_URL", "http://localhost:8000")
    
    # 모델 설정
    MODEL_NAME: str = os.getenv("MODEL_NAME", "BM-K/KoSimCSE-roberta")
    
    # CORS 설정
    CORS_ORIGINS: list = os.getenv("CORS_ORIGINS", "*").split(",")
    CORS_ALLOW_CREDENTIALS: bool = os.getenv("CORS_ALLOW_CREDENTIALS", "true").lower() == "true"
    CORS_ALLOW_METHODS: list = os.getenv("CORS_ALLOW_METHODS", "*").split(",")
    CORS_ALLOW_HEADERS: list = os.getenv("CORS_ALLOW_HEADERS", "*").split(",")
    
    # HTTP 클라이언트 설정
    HTTP_TIMEOUT: float = float(os.getenv("HTTP_TIMEOUT", "60.0"))
    
    # 추천 설정
    TOP_PETSTARS_COUNT: int = int(os.getenv("TOP_PETSTARS_COUNT", "2"))
    
    # 모델 설정
    MAX_LENGTH: int = int(os.getenv("MAX_LENGTH", "512"))

# 전역 설정 인스턴스
settings = Settings()
