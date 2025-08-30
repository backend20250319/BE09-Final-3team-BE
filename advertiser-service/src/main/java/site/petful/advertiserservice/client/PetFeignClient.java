package site.petful.advertiserservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import site.petful.advertiserservice.config.FeignConfig;

@FeignClient(name = "pet-service", path = "/", configuration= FeignConfig.class)
public interface PetFeignClient {

    // 1. 펫스타 전체 목록 조회
}
