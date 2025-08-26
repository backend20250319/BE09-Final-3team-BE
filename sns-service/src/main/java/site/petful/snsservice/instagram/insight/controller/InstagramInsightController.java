package site.petful.snsservice.instagram.insight.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.instagram.insight.dto.InstagramEngagementResponseDto;
import site.petful.snsservice.instagram.insight.dto.InstagramFollowerHistoryResponseDto;
import site.petful.snsservice.instagram.insight.dto.InstagramInsightResponseDto;
import site.petful.snsservice.instagram.insight.service.InstagramFollowerHistoryService;
import site.petful.snsservice.instagram.insight.service.InstagramInsightsService;

@Controller
@RequestMapping("/instagram/insights")
@RequiredArgsConstructor
public class InstagramInsightController {

    private final InstagramInsightsService instagramInsightsService;
    private final InstagramFollowerHistoryService instagramFollowerHistoryService;

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<Void>> syncInsights(
        @RequestParam("user_id") Long userId,
        @RequestParam("instagram_id") Long instagramId) {
        instagramInsightsService.syncInsights(instagramId, userId, 6);

        return ResponseEntity.ok(ApiResponseGenerator.success(null));
    }

    @GetMapping("/analysis-data")
    public ResponseEntity<ApiResponse<List<InstagramInsightResponseDto>>> getAnalysisData(
        @RequestParam("instagram_id") Long instagramId) {
        List<InstagramInsightResponseDto> insightsDto = instagramInsightsService.getAnalysisData(
            instagramId);

        return ResponseEntity.ok(ApiResponseGenerator.success(insightsDto));

    }

    @GetMapping("/follower-history")
    public ResponseEntity<ApiResponse<List<InstagramFollowerHistoryResponseDto>>> getFollowerHistory(
        @RequestParam("instagram_id") Long instagramId) {
        List<InstagramFollowerHistoryResponseDto> insightsDto = instagramFollowerHistoryService.findAllByInstagramIdRecently6Month(
            instagramId);

        return ResponseEntity.ok(ApiResponseGenerator.success(insightsDto));

    }

    @GetMapping("/engagement")
    public ResponseEntity<ApiResponse<InstagramEngagementResponseDto>> getEngagementData(
        @RequestParam("instagram_id") Long instagramId) {
        InstagramEngagementResponseDto engagementData = instagramInsightsService.getEngagementData(
            instagramId);

        return ResponseEntity.ok(ApiResponseGenerator.success(engagementData));

    }
}
