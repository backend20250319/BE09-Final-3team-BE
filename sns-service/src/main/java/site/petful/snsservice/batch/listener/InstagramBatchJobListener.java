package site.petful.snsservice.batch.listener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InstagramBatchJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("=== Instagram 배치 작업 시작 ===");
        log.info("작업 ID: {}", jobExecution.getJobId());
        log.info("작업 이름: {}", jobExecution.getJobInstance().getJobName());
        log.info("시작 시간: {}",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.info("작업 파라미터: {}", jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("=== Instagram 배치 작업 완료 ===");
        log.info("작업 ID: {}", jobExecution.getJobId());
        log.info("작업 이름: {}", jobExecution.getJobInstance().getJobName());
        log.info("완료 시간: {}",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.info("종료 상태: {}", jobExecution.getStatus());
        log.info("시작 시간: {}", jobExecution.getStartTime());
        log.info("종료 시간: {}", jobExecution.getEndTime());

        if (jobExecution.getEndTime() != null && jobExecution.getStartTime() != null) {
            long executionTime =
                jobExecution.getEndTime().getNano() - jobExecution.getStartTime().getNano();
            log.info("총 실행 시간: {}ms ({}초)", executionTime, executionTime * 1000);
        }

        if (jobExecution.getStatus().isUnsuccessful()) {
            log.error("배치 작업 실패 - 종료 상태: {}", jobExecution.getStatus());
            log.error("실패 원인: {}", jobExecution.getAllFailureExceptions());
        } else {
            log.info("배치 작업 성공적으로 완료되었습니다.");
        }
    }
}
