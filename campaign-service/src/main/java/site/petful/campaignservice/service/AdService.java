package site.petful.campaignservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.campaignservice.client.AdvertiserFeignClient;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.dto.advertisement.AdsGroupedResponse;
import site.petful.campaignservice.repository.AdRepository;

@Service
@RequiredArgsConstructor
public class AdService {

    private final AdRepository adRepository;
    private final AdvertiserFeignClient advertiserFeignClient;

    // 1. adStatus별(모집중/종료된) 광고(캠페인) 전체 조회
    public AdsGroupedResponse getAdsByStatusGrouped() {
        ApiResponse<AdsGroupedResponse> response = advertiserFeignClient.getAdsGroupedByAdStatus();
        return response.getData();
    }

}
