package site.petful.advertiserservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.dto.advertisement.AdResponse;
import site.petful.advertiserservice.dto.advertisement.AdsGroupedResponse;
import site.petful.advertiserservice.dto.advertisement.AdsResponse;
import site.petful.advertiserservice.dto.advertisement.ImageUploadResponse;
import site.petful.advertiserservice.dto.advertiser.FileUploadResponse;
import site.petful.advertiserservice.security.SecurityUtil;
import site.petful.advertiserservice.service.AdService;
import site.petful.advertiserservice.service.FileService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/internal")
public class InternalController {

    private final AdService adService;
    private final FileService fileService;

    public InternalController(AdService adService, FileService fileService) {
        this.adService = adService;
        this.fileService = fileService;
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

    // 2-4. adStatus별(모집중/종료된) 광고(캠페인) 전체 조회 - 체험단
    @GetMapping("/adStatus/grouped")
    public ResponseEntity<ApiResponse<?>> getAllAdsByAdStatusGrouped() {
        try {
            AdsGroupedResponse response = adService.getAllAdsByAdStatusGrouped();
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_INTERNAL_SERVER_ERROR));
        }
    }

    // 2-5. List<Long> adNo에 대한 광고(캠페인) 조회 - 체험단
    @PostMapping("/adNos")
    public ResponseEntity<ApiResponse<?>> getAdsByAdNos(@RequestBody List<Long> adNos) {
        try {
            AdsResponse response = adService.getAdsByAdNos(adNos);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_INTERNAL_SERVER_ERROR));
        }
    }

    // 3-2. 광고(캠페인) 수정 - 체험단
    @PutMapping("/campaign/{adNo}")
    public ResponseEntity<ApiResponse<?>> updateAdByCampaign(
            @PathVariable Long adNo,
            @RequestParam Integer incrementBy){
        try {
            AdResponse response = adService.updateAdByCampaign(adNo, incrementBy);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }

    /* 파일/이미지 API */
    // 2-1. 광고 이미지 조회
    @GetMapping("/ad/{adNo}")
    public ResponseEntity<ApiResponse<?>> getImageByAdNo(@PathVariable Long adNo) {
        try {
            ImageUploadResponse response = fileService.getImageByAdNo(adNo);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }

    // 2-2. 광고주 파일 조회
    @GetMapping("/advertiser/{advertiserNo}")
    public ResponseEntity<ApiResponse<?>> getFileByAdvertiserNo(@PathVariable Long advertiserNo) {
        log.info("🔍 [InternalController] 광고주 파일 조회 요청: advertiserNo={}", advertiserNo);
        try {
            List<FileUploadResponse> response = fileService.getFileByAdvertiserNo(advertiserNo);
            log.info("✅ [InternalController] 파일 조회 성공: advertiserNo={}, fileCount={}", advertiserNo, response.size());
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            log.error("❌ [InternalController] 파일 조회 실패: advertiserNo={}, error={}", advertiserNo, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.ADVERTISER_NOT_FOUND));
        }
    }
}