package site.petful.snsservice.batch.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import site.petful.snsservice.instagram.auth.service.InstagramTokenService;


@Component
@RequiredArgsConstructor
@Slf4j
public class InstagramTokenCleanupTasklet implements Tasklet {

    private final InstagramTokenService instagramTokenService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception {
        log.info("==========Instagram Token 삭제 시작==========");

        try {
            int deletedCount = instagramTokenService.deleteExpiredTokens();
            log.info("==========Instagram token {}개 삭제 완료==========", deletedCount);
            return RepeatStatus.FINISHED;

        } catch (Exception e) {
            log.error("Instagram 프로필 동기화 중 치명적 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}
