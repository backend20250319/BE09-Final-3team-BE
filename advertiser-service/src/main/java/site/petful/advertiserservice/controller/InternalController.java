package site.petful.advertiserservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.dto.advertisement.AdResponse;
import site.petful.advertiserservice.dto.advertisement.AdsGroupedResponse;
import site.petful.advertiserservice.dto.advertisement.AdsResponse;
import site.petful.advertiserservice.service.AdService;

import java.util.List;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private final AdService adService;

    public InternalController(AdService adService) {
        this.adService = adService;
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
}