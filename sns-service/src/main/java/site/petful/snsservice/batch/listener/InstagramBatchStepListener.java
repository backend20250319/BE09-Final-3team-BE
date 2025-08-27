package site.petful.snsservice.batch.listener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InstagramBatchStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("=== Instagram 배치 단계 시작 ===");
        log.info("단계 이름: {}", stepExecution.getStepName());
        log.info("단계 ID: {}", stepExecution.getId());
        log.info("시작 시간: {}",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.info("작업 실행 ID: {}", stepExecution.getJobExecutionId());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("=== Instagram 배치 단계 완료 ===");
        log.info("단계 이름: {}", stepExecution.getStepName());
        log.info("단계 ID: {}", stepExecution.getId());
        log.info("완료 시간: {}",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.info("종료 상태: {}", stepExecution.getExitStatus());
        log.info("시작 시간: {}", stepExecution.getStartTime());
        log.info("종료 시간: {}", stepExecution.getEndTime());

        if (stepExecution.getEndTime() != null && stepExecution.getStartTime() != null) {
            long executionTime =
                stepExecution.getEndTime().getNano() - stepExecution.getStartTime().getNano();
            log.info("단계 실행 시간: {}ms ({}초)", executionTime, executionTime / 1000);
        }

        if (stepExecution.getStatus().isUnsuccessful()) {
            log.error("배치 단계 실패 - 종료 상태: {}", stepExecution.getExitStatus());
            log.error("실패 원인: {}", stepExecution.getFailureExceptions());
        } else {
            log.info("배치 단계 성공적으로 완료되었습니다.");
        }

        return stepExecution.getExitStatus();
    }
}
