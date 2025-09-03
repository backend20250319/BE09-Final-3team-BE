package site.petful.campaignservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.dto.HistoryImageInfo;
import site.petful.campaignservice.dto.HistoryResponse;
import site.petful.campaignservice.dto.PetResponse;
import site.petful.campaignservice.dto.PortfolioResponse;

import java.util.List;

@FeignClient(name = "petFeignClient", url = "http://localhost:8000/api/v1/pet-service")
public interface PetFeignClient{

    // 1. 반려동물 상세 조회
    @GetMapping("/pets/{petNo}")
    ApiResponse<PetResponse> getPet(@PathVariable Long petNo);

    // 2. 반려동물 목록 조회
    @GetMapping("/pets/external")
    ApiResponse<List<PetResponse>> getPetsExternal(@RequestParam Long userNo);

    // 3. petNos 리스트로 펫 조회
    @PostMapping("/petsByPetNos")
    ApiResponse<List<PetResponse>> getPetsByPetNos(@RequestBody List<Long> petNos);

    // 4. 포트폴리오 조회
    @GetMapping("/pets/{petNo}/portfolio/external")
    ApiResponse<PortfolioResponse> getPortfolioExternal(@PathVariable Long petNo);

    // 5. 활동이력 조회
    @GetMapping("/pets/{petNo}/histories/external")
    ApiResponse<List<HistoryResponse>> getHistoriesExternal(@PathVariable Long petNo);

    // 6. 활동이력 이미지 조회
    @GetMapping("/pets/{petNo}/histories/external")
    ApiResponse<List<HistoryImageInfo>> getHistoryImagesExternal(@PathVariable Long petNo);

}
