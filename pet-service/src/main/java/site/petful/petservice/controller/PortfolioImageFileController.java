package site.petful.petservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.petful.petservice.dto.PortfolioImageFileResponse;
import site.petful.petservice.service.PortfolioImageFileService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/histories/{historyNo}/images")
@RequiredArgsConstructor
@Slf4j
public class PortfolioImageFileController {

    private final PortfolioImageFileService portfolioImageFileService;

    /**
     * 활동이력 이미지 업로드 (단일)
     * POST /histories/{historyNo}/images
     */
    @PostMapping
    public ResponseEntity<PortfolioImageFileResponse> uploadHistoryImage(
            @PathVariable Long historyNo,
            @RequestParam("file") MultipartFile file) {
        try {
            PortfolioImageFileResponse response = portfolioImageFileService.uploadHistoryImage(historyNo, file);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 활동이력 이미지 업로드 (다중)
     * POST /histories/{historyNo}/images/multiple
     */
    @PostMapping("/multiple")
    public ResponseEntity<List<PortfolioImageFileResponse>> uploadHistoryImages(
            @PathVariable Long historyNo,
            @RequestParam("files") List<MultipartFile> files) {
        try {
            List<PortfolioImageFileResponse> responses = portfolioImageFileService.uploadHistoryImages(historyNo, files);
            return ResponseEntity.ok(responses);
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 특정 활동이력의 모든 이미지 조회
     * GET /histories/{historyNo}/images
     */
    @GetMapping
    public ResponseEntity<List<PortfolioImageFileResponse>> getHistoryImages(@PathVariable Long historyNo) {
        try {
            List<PortfolioImageFileResponse> images = portfolioImageFileService.getHistoryImages(historyNo);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            log.error("이미지 조회 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 반려동물의 모든 활동이력 이미지 조회
     * GET /pets/{petNo}/history-images
     */
    @GetMapping("/pets/{petNo}/history-images")
    public ResponseEntity<List<PortfolioImageFileResponse>> getPetHistoryImages(@PathVariable Long petNo) {
        try {
            List<PortfolioImageFileResponse> images = portfolioImageFileService.getPetHistoryImages(petNo);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            log.error("반려동물 활동이력 이미지 조회 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 활동이력 이미지 삭제
     * DELETE /histories/{historyNo}/images/{fileId}
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteHistoryImage(
            @PathVariable Long historyNo,
            @PathVariable Long fileId) {
        try {
            portfolioImageFileService.deleteHistoryImage(fileId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("파일 삭제 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
