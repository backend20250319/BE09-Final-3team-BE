package site.petful.petservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.petservice.common.ApiResponse;
import site.petful.petservice.dto.PortfolioRequest;
import site.petful.petservice.dto.PortfolioResponse;
import site.petful.petservice.service.PortfolioService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class PortfolioController {

    private final PortfolioService portfolioService;

    // 포트폴리오 생성
    @PostMapping("/pets/{petNo}/portfolio")
    public ResponseEntity<ApiResponse<PortfolioResponse>> createPortfolio(
            @PathVariable Long petNo,
            @RequestAttribute("X-User-No") Long userNo,
            @RequestBody PortfolioRequest request) {
        PortfolioResponse response = portfolioService.createPortfolio(petNo, userNo, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 포트폴리오 조회
    @GetMapping("/pets/{petNo}/portfolio")
    public ResponseEntity<ApiResponse<PortfolioResponse>> getPortfolio(
            @PathVariable Long petNo,
            @RequestAttribute("X-User-No") Long userNo) {
        PortfolioResponse response = portfolioService.getPortfolio(petNo, userNo);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 사용자의 모든 포트폴리오 조회
    @GetMapping("/portfolios")
    public ResponseEntity<ApiResponse<List<PortfolioResponse>>> getPortfolios(
            @RequestAttribute("X-User-No") Long userNo) {
        List<PortfolioResponse> portfolios = portfolioService.getPortfoliosByUser(userNo);
        return ResponseEntity.ok(ApiResponse.success(portfolios));
    }

    // 포트폴리오 수정
    @PutMapping("/pets/{petNo}/portfolio")
    public ResponseEntity<ApiResponse<PortfolioResponse>> updatePortfolio(
            @PathVariable Long petNo,
            @RequestAttribute("X-User-No") Long userNo,
            @RequestBody PortfolioRequest request) {
        PortfolioResponse response = portfolioService.updatePortfolio(petNo, userNo, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 포트폴리오 삭제
    @DeleteMapping("/pets/{petNo}/portfolio")
    public ResponseEntity<ApiResponse<Void>> deletePortfolio(
            @PathVariable Long petNo,
            @RequestAttribute("X-User-No") Long userNo) {
        portfolioService.deletePortfolio(petNo, userNo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 임시저장 포트폴리오 조회
    @GetMapping("/portfolios/saved")
    public ResponseEntity<ApiResponse<List<PortfolioResponse>>> getSavedPortfolios(
            @RequestAttribute("X-User-No") Long userNo) {
        List<PortfolioResponse> savedPortfolios = portfolioService.getSavedPortfolios(userNo);
        return ResponseEntity.ok(ApiResponse.success(savedPortfolios));
    }
}

    