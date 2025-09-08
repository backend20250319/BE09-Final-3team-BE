package site.petful.advertiserservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import site.petful.advertiserservice.service.RecommendationService;

@RestController
@RequestMapping("recommend")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    // 펫스타 추천
    @GetMapping("/petStars")
    public Mono<String> getPetStars(@RequestHeader("Authorization") String authorizationHeader) {
        return recommendationService.getPetStars(authorizationHeader);
    }
}
