package site.petful.snsservice.batch.scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
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
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    @SchedulerLock(
        name = "dailySyncAndCleanUpBatchLock",
        lockAtMostFor = "PT50M",            // (2) 최대 잠금 유지 시간
        lockAtLeastFor = "PT1M"             // (3) 최소 잠금 유지 시간
    )
    public void runInstagramSyncJob() {
        log.info("=== [Scheduled] 배치 작업 시작 - {}",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        instagramBatchService.runInstagramTokenCleanupBatch();

        instagramBatchService.runInstagramSyncBatchAsync(1L);

        log.info("=== [Scheduled] 배치 작업 종료 - {}",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

}
