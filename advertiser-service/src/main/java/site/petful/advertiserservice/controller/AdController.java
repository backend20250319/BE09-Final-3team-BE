package site.petful.advertiserservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.dto.advertisement.AdRequest;
import site.petful.advertiserservice.dto.advertisement.AdResponse;
import site.petful.advertiserservice.dto.advertisement.AdsResponse;
import site.petful.advertiserservice.entity.advertisement.AdStatus;
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

    // 2-1. 광고(캠페인) 단일 조회
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

    // 2-2. 광고(캠페인) 전체 조회

    // 2-3. 광고주별 광고(캠페인) 전체 조회 (+ adStatus에 따라 필터링 적용)
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllAds(@RequestParam Long advertiserNo,
                                                    @RequestParam(required = false) AdStatus adStatus) {
        try {
            AdsResponse response = adService.getAllAds(advertiserNo, adStatus);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            if(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage().equals(errorMessage)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseGenerator.fail(ErrorCode.ADVERTISER_NOT_FOUND));
            } else if (ErrorCode.AD_NOT_MATCHED.getDefaultMessage().equals(errorMessage)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_MATCHED));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_INTERNAL_SERVER_ERROR));
        }
    }

    // 2-4. adStatus별 광고(캠페인) 전체 조회

    // 3-1. 광고(캠페인) 수정 - 광고주
    @PutMapping("/{adNo}")
    public ResponseEntity<ApiResponse<?>> updateAd(
            @PathVariable Long adNo,
            @RequestBody AdRequest request) {
        try {
            AdResponse response = adService.updateAd(adNo, request);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }

    // 3-2. 광고(캠페인) 수정 (AdStatus: APPROVED/REJECTED, (선택:반려 사유 추가)) - 관리자

    // 4. 광고(캠페인) 삭제
    @DeleteMapping("/{adNo}")
    public ResponseEntity<ApiResponse<?>>  deleteAd(@PathVariable Long adNo) {
        try{
            adService.deleteAd(adNo);
            return ResponseEntity.ok(ApiResponseGenerator.success());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }
}
