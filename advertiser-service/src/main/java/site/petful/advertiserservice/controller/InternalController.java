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
import site.petful.advertiserservice.service.AdService;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final AdService adService;

    // 2-4. adStatus별(모집중/종료된) 광고(캠페인) 전체 조회 - 체험단
    @GetMapping("/adStatus/grouped")
    public ResponseEntity<ApiResponse<?>> getAllAdsByAdStatusGrouped() {
        AdsGroupedResponse response = adService.getAllAdsByAdStatusGrouped();
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
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