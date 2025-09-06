package site.petful.campaignservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.common.ApiResponseGenerator;
import site.petful.campaignservice.dto.HistoryImageInfo;
import site.petful.campaignservice.dto.HistoryResponse;
import site.petful.campaignservice.dto.pet.PetResponse;
import site.petful.campaignservice.dto.pet.PortfolioResponse;
import site.petful.campaignservice.security.SecurityUtil;
import site.petful.campaignservice.service.PetService;

import java.util.List;

@RestController
@RequestMapping("/pet")
public class PetController {

    private final PetService petService;
    private final SecurityUtil securityUtil;

    public PetController(PetService petService, SecurityUtil securityUtil) {
        this.petService = petService;
        this.securityUtil = securityUtil;
    }

    // 1. 사용자별 펫 조회
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getPets() {
        Long userNo = securityUtil.getCurrentUserNo();
        List<PetResponse> response = petService.getPets(userNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }

    // 2. 포트폴리오 조회
    @GetMapping("/portfolio/{petNo}")
    public ResponseEntity<ApiResponse<PortfolioResponse>> getPortfolioExternal(@PathVariable Long petNo) {
        PortfolioResponse response = petService.getPortfolio(petNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }

    // 3. 활동 이력 조회
    @GetMapping("/history/{petNo}")
    public ResponseEntity<ApiResponse<List<HistoryResponse>>> getHistoryExternal(@PathVariable Long petNo) {
        List<HistoryResponse> response = petService.getHistory(petNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }

    // 4. 활동 이력 이미지 조회
    @GetMapping("/history/images/{petNo}")
    public ResponseEntity<ApiResponse<List<HistoryImageInfo>>> getHistoryImagesExternal(@PathVariable Long petNo) {
        List<HistoryImageInfo> response = petService.getImages(petNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }
}
