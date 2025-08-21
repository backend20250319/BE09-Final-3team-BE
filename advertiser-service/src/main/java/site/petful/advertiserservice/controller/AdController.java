package site.petful.advertiserservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.dto.advertisement.AdRequest;
import site.petful.advertiserservice.dto.advertisement.AdResponse;
import site.petful.advertiserservice.service.AdService;

@RestController
@RequestMapping("/ad")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }

    // 1. 광고(캠페인) 생성
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createAd(
            @RequestParam Long advertiserNo,
            @RequestBody AdRequest request) {
        try {
            AdResponse response = adService.createAd(advertiserNo, request);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_INVALID_REQUEST));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.ADVERTISER_NOT_FOUND));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_INTERNAL_SERVER_ERROR));
        }
    }

    // 2. 광고(캠페인) 단일 조회
    @GetMapping("/{adNo}")
    public ResponseEntity<ApiResponse<?>> getAd(@PathVariable Long adNo) {
        try {
            AdResponse response = adService.getAd(adNo);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }
}
