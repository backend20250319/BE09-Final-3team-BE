package site.petful.snsservice.instagram.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import site.petful.snsservice.instagram.domain.InstagramToken;
import site.petful.snsservice.instagram.dto.InstagramTokenResponse;
import site.petful.snsservice.instagram.repository.InstagramRepository;

@Service
public class InstagramService {

    private final InstagramRepository instagramRepository;
    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;

    public InstagramService(InstagramRepository instagramRepository, WebClient webClient,
        @Value("${instagram.api.client_id}") String clientId,
        @Value("${instagram.api.client_secret}") String clientSecret) {
        this.instagramRepository = instagramRepository;
        this.webClient = webClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public void connect(String token) {
        // WebClient를 사용하여 API를 호출합니다.
        System.out.println(clientId);
        System.out.println(clientSecret);

        InstagramTokenResponse instagramTokenResponse = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/oauth/access_token") // 요청할 세부 경로
                .queryParam("grant_type", "fb_exchange_token")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("fb_exchange_token", token) // 액세스 토큰 추가
                .build())
            .retrieve() // 요청을 실행하고 응답을 받습니다.
            .bodyToMono(InstagramTokenResponse.class) // 응답 본문을 String 형태로 변환합니다.
            .block();

        //TODO 여기 userId 수정
        InstagramToken instagramToken = new InstagramToken(1L, instagramTokenResponse);
        instagramRepository.save(instagramToken);
    }
}