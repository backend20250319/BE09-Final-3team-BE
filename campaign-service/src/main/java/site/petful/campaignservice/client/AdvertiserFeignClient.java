package site.petful.campaignservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.dto.advertisement.AdsGroupedResponse;

@FeignClient(name = "advertiser-service", url = "http://localhost:8000/api/v1/advertiser-service/internal")
public interface AdvertiserFeignClient {

    // 1. adStatus별(모집중/종료된) 광고(캠페인) 전체 조회
    @GetMapping("/adStatus/grouped")
    ApiResponse<AdsGroupedResponse> getAdsGroupedByAdStatus();

    // 2. 광고(캠페인 수정) : applicants 1 증가 - 체험단
    @PutMapping("/campaign/{adNo}")
    ApiResponse<Void> updateAdByCampaign(@PathVariable Long adNo, @RequestParam Integer incrementBy);
}
