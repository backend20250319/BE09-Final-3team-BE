package site.petful.advertiserservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.config.FeignConfig;
import site.petful.advertiserservice.dto.campaign.PetResponse;

import java.util.List;

@FeignClient(name = "pet-service", path = "/", configuration= FeignConfig.class)
public interface PetFeignClient {

    // 1. 펫스타 전체 목록 조회
    @GetMapping("/petstars")
    ApiResponse<List<PetResponse>> getAllPetStars();
}
