package site.petful.petservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.petful.petservice.common.ApiResponse;
import site.petful.petservice.dto.HistoryRequest;
import site.petful.petservice.dto.HistoryResponse;
import site.petful.petservice.dto.MultipleFileUploadResponse;
import site.petful.petservice.service.HistoryService;

import java.util.List;

@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    // 활동이력 생성
    @PostMapping("/{petNo}/histories")
    public ResponseEntity<ApiResponse<HistoryResponse>> createHistory(
            @PathVariable Long petNo,
            @RequestAttribute("X-User-No") Long userNo,
            @RequestBody HistoryRequest request) {
        HistoryResponse response = historyService.createHistory(petNo, userNo, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 활동이력 조회
    @GetMapping("/{petNo}/histories/{historyNo}")
    public ResponseEntity<ApiResponse<HistoryResponse>> getHistory(
            @PathVariable Long petNo,
            @PathVariable Long historyNo,
            @RequestAttribute("X-User-No") Long userNo) {
        HistoryResponse response = historyService.getHistory(petNo, historyNo, userNo);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 반려동물의 모든 활동이력 조회
    @GetMapping("/{petNo}/histories")
    public ResponseEntity<ApiResponse<List<HistoryResponse>>> getHistories(
            @PathVariable Long petNo,
            @RequestAttribute("X-User-No") Long userNo) {
        List<HistoryResponse> responses = historyService.getHistories(petNo, userNo);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 활동이력 수정
    @PutMapping("/{petNo}/histories/{historyNo}")
    public ResponseEntity<ApiResponse<HistoryResponse>> updateHistory(
            @PathVariable Long petNo,
            @PathVariable Long historyNo,
            @RequestAttribute("X-User-No") Long userNo,
            @RequestBody HistoryRequest request) {
        HistoryResponse response = historyService.updateHistory(petNo, historyNo, userNo, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 활동이력 삭제
    @DeleteMapping("/{petNo}/histories/{historyNo}")
    public ResponseEntity<ApiResponse<Void>> deleteHistory(
            @PathVariable Long petNo,
            @PathVariable Long historyNo,
            @RequestAttribute("X-User-No") Long userNo) {
        historyService.deleteHistory(petNo, historyNo, userNo);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 활동이력 이미지 업로드
    @PostMapping("/{petNo}/histories/{historyNo}/images")
    public ResponseEntity<ApiResponse<MultipleFileUploadResponse>> uploadHistoryImages(
            @PathVariable Long petNo,
            @PathVariable Long historyNo,
            @RequestAttribute("X-User-No") Long userNo,
            @RequestParam("files") List<MultipartFile> files) {
        
        MultipleFileUploadResponse response = historyService.uploadHistoryImages(files, petNo, historyNo, userNo);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(response));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(response.getMessage()));
        }
    }

}

