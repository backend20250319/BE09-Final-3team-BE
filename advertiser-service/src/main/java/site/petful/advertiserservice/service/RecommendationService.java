package site.petful.advertiserservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import site.petful.advertiserservice.dto.advertisement.AdResponse;

@Service
public class RecommendationService {

    private final WebClient webClient;
    private final AdService adService;

    public RecommendationService(WebClient.Builder webClientBuilder, AdService adService) {
        this.webClient = webClientBuilder
                .baseUrl(System.getenv()
                .getOrDefault("GATEWAY_URL", "http://localhost:8000"))
                .build();
        this.adService = adService;
    }

    // 펫스타 추천
    public Mono<String> getPetStars(String token, Long adNo) {

        AdResponse adInfo = adService.getAd(adNo);

        return webClient.post()
                .uri("/recommendation/hello") // 이후 pets로 수정
                .header("Authorization", token)
                .body(BodyInserters.fromValue(adInfo))
                .retrieve()
                .bodyToMono(String.class);
    }
}
