package site.petful.campaignservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.dto.advertisement.*;

import java.util.List;

@FeignClient(name = "advertiser-service", path = "/internal")
public interface AdvertiserFeignClient {

    // 1. 광고 단일 조회
    @GetMapping("/{adNo}")
    ApiResponse<AdResponse> getAd(@PathVariable Long adNo);

    // 2. adStatus별(모집중/종료된) 광고(캠페인) 전체 조회
    @GetMapping("/adStatus/grouped")
    ApiResponse<AdsGroupedResponse> getAllAdsByAdStatusGrouped();

    // 3. List<Long> adNo에 대한 광고(캠페인) 조회
    @PostMapping("/adNos")
    ApiResponse<AdsResponse> getAdsByAdNos(List<Long> adNos);

    // 4. 광고(캠페인 수정) : applicants 수정
    @PutMapping("/campaign/{adNo}")
    ApiResponse<Void> updateAdByCampaign(@PathVariable Long adNo, @RequestParam Integer incrementBy);

    // 5. 광고 이미지 조회
    @GetMapping("/ad/{adNo}")
    ApiResponse<ImageUploadResponse> getImageByAdNo(@PathVariable Long adNo);

    // 6. 광고주 파일 조회
    @GetMapping("/advertiser/{advertiserNo}")
    ApiResponse<List<FileUploadResponse>> getFileByAdvertiserNo(@PathVariable Long advertiserNo);

}
