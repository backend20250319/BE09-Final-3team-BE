package site.petful.snsservice.batch.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.snsservice.batch.service.InstagramBatchService;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.common.ErrorCode;

@RestController
@RequestMapping("/batch/instagram")
@RequiredArgsConstructor
@Slf4j
public class InstagramBatchController {

    private final InstagramBatchService instagramBatchService;


    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<?>> runInstagramSyncBatch() {
        log.info("==========수동 Instagram 동기화 배치 작업 시작==========");

        try {
            // 비동기로 배치 작업 실행
            instagramBatchService.runInstagramSyncBatchAsync(1L)
                .thenAccept(response -> log.info("Instagram 동기화 배치 작업 완료: {}", response))
                .exceptionally(throwable -> {
                    log.error("Instagram 동기화 배치 작업 실행 중 오류 발생: {}", throwable.getMessage());
                    return null;
                });

            Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Instagram 동기화 배치 작업이 비동기로 시작되었습니다. (기본값: 6개월)",
                "executionTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "executionType", "manual_async",
                "monthsToSync", 6
            );

            return ResponseEntity.ok(ApiResponseGenerator.success(response));

        } catch (Exception e) {
            log.error("Instagram 배치 작업 실행 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST));
        }
    }

    @PostMapping("/sync/user/{userNo}")
    public ResponseEntity<ApiResponse<?>> runInstagramSyncBatchForUser(
        @PathVariable Long userNo) {
        log.info("========== 사용자 {}에 대한 수동 Instagram 동기화 배치 작업 시작 ========== ", userNo);

        try {
            // 비동기로 배치 작업 실행
            instagramBatchService.runInstagramSyncBatchForUserAsync(userNo, 6L)
                .thenAccept(
                    response -> log.info("========== 사용자 {} Instagram 동기화 배치 작업 완료: {} ========== ",
                        userNo, response))
                .exceptionally(throwable -> {
                    log.error("사용자 {} Instagram 동기화 배치 작업 실행 중 오류 발생: {}", userNo,
                        throwable.getMessage());
                    return null;
                });

            Map<String, Object> response = Map.of(
                "status", "success",
                "message",
                String.format("사용자 %d에 대한 Instagram 동기화 배치 작업이 비동기로 시작되었습니다. (기본값: 6개월)", userNo),
                "executionTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "executionType", "manual_async",
                "targetUserNo", userNo,
                "monthsToSync", 6
            );

            return ResponseEntity.ok(ApiResponseGenerator.success(response));

        } catch (Exception e) {
            log.error("사용자 {}에 대한 Instagram 배치 작업 실행 중 오류 발생: {}", userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBatchStatus() {
        Map<String, Object> status = Map.of(
            "batchName", "Instagram 동기화 배치",
            "description", "인스타그램 프로필, 미디어, 댓글, 인사이트 데이터를 동기화하는 배치 작업",
            "schedule", "매일 새벽 2시",
            "lastExecution",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            "status", "active",
            "executionType", "비동기 처리 지원"
        );

        return ResponseEntity.ok(ApiResponseGenerator.success(status));
    }
}
