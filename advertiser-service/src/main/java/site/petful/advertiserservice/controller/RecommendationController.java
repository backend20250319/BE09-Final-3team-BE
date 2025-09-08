package site.petful.advertiserservice.controller;

import org.springframework.web.bind.annotation.*;
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
    @PostMapping("/petStars/{adNo}")
    public Mono<String> getPetStars(@RequestHeader("Authorization") String authorizationHeader,
                                    @PathVariable Long adNo) {
        return recommendationService.getPetStars(authorizationHeader, adNo);
    }
}
