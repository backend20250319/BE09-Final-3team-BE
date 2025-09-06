package site.petful.advertiserservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.dto.advertiser.AdvertiserRequest;
import site.petful.advertiserservice.dto.advertiser.AdvertiserResponse;
import site.petful.advertiserservice.security.SecurityUtil;
import site.petful.advertiserservice.service.AdvertiserService;

@RestController
@RequestMapping("/advertiser")
public class AdvertiserController {

    private final AdvertiserService advertiserService;
    private final SecurityUtil securityUtil;

    public AdvertiserController(AdvertiserService advertiserService, SecurityUtil securityUtil) {
        this.advertiserService = advertiserService;
        this.securityUtil = securityUtil;
    }

    // 1. 광고주 프로필 정보 조회
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getAdvertiser(@AuthenticationPrincipal String advertiserNo) {
        try {
            AdvertiserResponse response = advertiserService.getAdvertiser(Long.valueOf(advertiserNo));
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.ADVERTISER_NOT_FOUND));
        }
    }

    // 2. 광고주 프로필 정보 수정
    @PutMapping(value = "/profile")
    public ResponseEntity<ApiResponse<?>> updateAdvertiser(@AuthenticationPrincipal String advertiserNo, @RequestBody AdvertiserRequest updateRequest) {
        try {
            AdvertiserResponse updatedResponse = advertiserService.updateAdvertiser(Long.valueOf(advertiserNo), updateRequest);
            return ResponseEntity.ok(ApiResponseGenerator.success(updatedResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.ADVERTISER_NOT_FOUND));
        }
    }
}