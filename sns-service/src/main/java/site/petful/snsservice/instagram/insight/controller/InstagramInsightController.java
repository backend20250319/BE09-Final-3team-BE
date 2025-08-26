package site.petful.snsservice.instagram.insight.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.instagram.insight.service.InstagramInsightsService;

@Controller
@RequestMapping("/instagram/insights")
@RequiredArgsConstructor
public class InstagramInsightController {

    private final InstagramInsightsService instagramInsightsService;

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<Void>> syncInstagramInsights(
        @RequestParam("user_id") Long userId,
        @RequestParam("instagram_id") Long instagramId) {
        instagramInsightsService.syncInsightRecentSixMonth(instagramId, userId);

        return ResponseEntity.ok(ApiResponseGenerator.success(null));
    }


}
