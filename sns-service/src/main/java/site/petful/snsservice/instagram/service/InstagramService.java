package site.petful.snsservice.instagram.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class InstagramService {

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;

    public InstagramService(WebClient webClient,
        @Value("${instagram.api.client_id}") String clientId,
        @Value("${instagram.api.client_secret}") String clientSecret) {

        this.webClient = webClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String connect(String token) {
        // WebClient를 사용하여 API를 호출합니다.
        System.out.println(clientId);
        System.out.println(clientSecret);
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/oauth/access_token") // 요청할 세부 경로
                .queryParam("grant_type", "fb_exchange_token")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("fb_exchange_token", token) // 액세스 토큰 추가
                .build())
            .retrieve() // 요청을 실행하고 응답을 받습니다.
            .bodyToMono(String.class) // 응답 본문을 String 형태로 변환합니다.
            .block();

    }
}