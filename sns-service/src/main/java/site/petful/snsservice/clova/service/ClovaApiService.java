package site.petful.snsservice.clova.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.snsservice.clova.client.ClovaApiClient;
import site.petful.snsservice.clova.client.dto.ClovaRequestDto;
import site.petful.snsservice.clova.client.dto.ClovaRequestDto.ContentPart;
import site.petful.snsservice.clova.client.dto.ClovaRequestDto.Message;
import site.petful.snsservice.clova.client.dto.ClovaResponseDto;
import site.petful.snsservice.instagram.comment.entity.Sentiment;

@Service
@RequiredArgsConstructor
public class ClovaApiService {

    private final ClovaApiClient clovaApiClient;

    public Sentiment analyzeSentiment(String userText) {
        String systemPrompt = """
                # 역할
                당신은 문장의 감정을 분석하는 시스템입니다. 당신의 유일한 임무는 문장을 읽고 '긍정', '부정', '중립' 세 가지 중 하나를 선택하는 것입니다.
            
                # 규칙
                1. 답변은 반드시 '긍정', '부정', '중립' 중 하나여야 합니다.
                2. '혼합' 또는 다른 단어를 절대 사용하지 마세요.
                3. 어떠한 설명도 추가하지 말고, 오직 하나의 단어만 출력하세요.
                4. 아무 뜻이 없으면 중립으로 출력
            
                # 예시
                - 문장: "기분 진짜 좋다" -> 긍정
                - 문장: "아오 진짜 짜증나게 하네" -> 부정
                - 문장: "이걸로 보내드릴게요" -> 중립
            
                # 지시
                다음 문장의 감정을 위 규칙에 따라 분석하세요.
            """;
        Message systemMessage = new Message("system",
            List.of(new ContentPart("text", systemPrompt)));

        String userPrompt = "문장: " + userText;
        Message userMessage = new Message("user", List.of(new ContentPart("text", userPrompt)));

        List<Message> messages = List.of(systemMessage, userMessage);

        ClovaRequestDto request = ClovaRequestDto.defaultRequest(messages);

        ClovaResponseDto response = clovaApiClient.getSentiment(request);

        // 5. 결과에서 필요한 텍스트만 추출하여 반환
        // 실제 응답 구조에 따라 반환 로직은 달라질 수 있습니다.
        String content = response.getResult().getMessage().getContent().trim();
        if (content.equals("긍정")) {
            return Sentiment.POSITIVE;
        } else if (content.equals("중립")) {
            return Sentiment.NEGATIVE;
        }

        return Sentiment.NEUTRAL;
    }
}