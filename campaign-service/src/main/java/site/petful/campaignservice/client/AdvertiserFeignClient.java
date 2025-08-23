package site.petful.campaignservice.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "advertiser-service", url = "http://localhost:8000/api/v1/advertiser-service")
public interface AdvertiserFeignClient {

    // 1. adStatus별 광고(캠페인) 전체 조회

}
