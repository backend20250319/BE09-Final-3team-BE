package site.petful.advertiserservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RecommendationService {

    private final WebClient webClient;

    public RecommendationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(System.getenv()
                .getOrDefault("GATEWAY_URL", "http://localhost:8000"))
                .build();
    }

    // 펫스타 추천
    public Mono<String> getPetStars(String token) {
        return webClient.get()
                .uri("/recommendation/hello") // 이후 pets로 수정
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(String.class);
    }
}
