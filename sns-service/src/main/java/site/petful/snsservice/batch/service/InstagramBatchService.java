package site.petful.snsservice.batch.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.common.ErrorCode;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstagramBatchService {

    private final JobLauncher jobLauncher;
    private final Job instagramSyncJob;

    @Async
    public CompletableFuture<ApiResponse<?>> runInstagramSyncBatchAsync(Long monthsToSync) {
        log.info("========== 비동기 Instagram 동기화 배치 작업 시작 ==========");

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("executionTime", LocalDateTime.now().toString())
                .addLong("timestamp", System.currentTimeMillis())
                .addLong("monthsToSync", monthsToSync) // 수동 실행 시 기본값: 6개월
                .toJobParameters();

            jobLauncher.run(instagramSyncJob, jobParameters);

            Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Instagram 동기화 배치 작업이 성공적으로 시작되었습니다. (기본값: 6개월)",
                "executionTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "executionType", "manual",
                "monthsToSync", 6
            );

            log.info("========== Instagram 동기화 배치 작업 비동기 실행 완료 ==========");
            return CompletableFuture.completedFuture(ApiResponseGenerator.success(response));

        } catch (JobExecutionAlreadyRunningException e) {
            log.error("Instagram 배치 작업이 이미 실행 중입니다: {}", e.getMessage());
            return CompletableFuture.completedFuture(
                ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST));
        } catch (JobRestartException e) {
            log.error("Instagram 배치 작업 재시작 실패: {}", e.getMessage());
            return CompletableFuture.completedFuture(
                ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST));
        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("Instagram 배치 작업 인스턴스가 이미 완료되었습니다: {}", e.getMessage());
            return CompletableFuture.completedFuture(
                ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST));
        } catch (JobParametersInvalidException e) {
            log.error("Instagram 배치 작업 파라미터가 유효하지 않습니다: {}", e.getMessage());
            return CompletableFuture.completedFuture(
                ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST));
        } catch (Exception e) {
            log.error("Instagram 배치 작업 실행 중 오류 발생: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(
                ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST));
        }
    }

    @Async
    public CompletableFuture<ApiResponse<?>> runInstagramSyncBatchForUserAsync(Long userNo,
        Long monthsToSync) {
        log.info("사용자 {}에 대한 비동기 Instagram 동기화 배치 작업 시작", userNo);

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("executionTime", LocalDateTime.now().toString())
                .addLong("timestamp", System.currentTimeMillis())
                .addLong("userNo", userNo)
                .addLong("monthsToSync", monthsToSync) // 수동 실행 시 기본값: 6개월
                .toJobParameters();

            jobLauncher.run(instagramSyncJob, jobParameters);

            Map<String, Object> response = Map.of(
                "status", "success",
                "message",
                String.format("사용자 %d에 대한 Instagram 동기화 배치 작업이 성공적으로 시작되었습니다. (기본값: 6개월)", userNo),
                "executionTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "executionType", "manual",
                "targetUserNo", userNo,
                "monthsToSync", 6
            );

            log.info("사용자 {} Instagram 동기화 배치 작업 비동기 실행 완료", userNo);
            return CompletableFuture.completedFuture(ApiResponseGenerator.success(response));

        } catch (Exception e) {
            log.error("사용자 {}에 대한 Instagram 배치 작업 실행 중 오류 발생: {}", userNo, e.getMessage(), e);
            return CompletableFuture.completedFuture(
                ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST));
        }
    }
}
