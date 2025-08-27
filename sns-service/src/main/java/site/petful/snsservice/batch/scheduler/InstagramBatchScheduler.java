package site.petful.snsservice.batch.scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.petful.snsservice.batch.service.InstagramBatchService;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstagramBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job instagramSyncJob;
    private final InstagramBatchService instagramBatchService;

    // 매일 새벽 2시 실행
    @Scheduled(cron = "0 0 2 * * *")
    public void runInstagramSyncJob() {
        log.info("=== [Scheduled] Instagram 배치 작업 시작 - {}",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        instagramBatchService.runInstagramSyncBatchAsync(1L);
    }

}
