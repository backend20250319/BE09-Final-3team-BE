package site.petful.campaignservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.dto.PetResponse;

import java.util.List;

@FeignClient(name = "pet-service", path = "/")
public interface PetFeignClient{

    // 1. 반려동물 상세 조회
    @GetMapping("/pets/{petNo}")
    ApiResponse<PetResponse> getPet(@PathVariable Long petNo);

    // 2. 반려동물 목록 조회
    @GetMapping("/pets")
    ApiResponse<List<PetResponse>> getPets(@RequestParam Long userNo);
}
