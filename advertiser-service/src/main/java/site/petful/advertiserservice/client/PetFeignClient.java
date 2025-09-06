package site.petful.advertiserservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.config.FeignConfig;
import site.petful.advertiserservice.dto.campaign.HistoryImageInfo;
import site.petful.advertiserservice.dto.campaign.HistoryResponse;
import site.petful.advertiserservice.dto.campaign.PetResponse;
import site.petful.advertiserservice.dto.campaign.PortfolioResponse;

import java.util.List;

@FeignClient(name = "pet-service", path = "/", configuration= FeignConfig.class)
public interface PetFeignClient {

    // 1. 펫스타 전체 목록 조회
    @GetMapping("/petstars")
    ApiResponse<List<PetResponse>> getAllPetStars();

    // 2. 포트폴리오 조회
    @GetMapping("/pets/{petNo}/portfolio/external")
    ApiResponse<PortfolioResponse> getPortfolioExternal(@PathVariable Long petNo);

    // 3. 활동이력 조회
    @GetMapping("/pets/{petNo}/histories/external")
    ApiResponse<List<HistoryResponse>> getHistoriesExternal(@PathVariable Long petNo);

}
