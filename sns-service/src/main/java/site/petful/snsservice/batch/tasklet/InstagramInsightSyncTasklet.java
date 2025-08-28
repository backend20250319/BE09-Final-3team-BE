package site.petful.snsservice.batch.tasklet;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import site.petful.snsservice.instagram.auth.service.InstagramTokenService;
import site.petful.snsservice.instagram.insight.service.InstagramInsightsService;
import site.petful.snsservice.instagram.profile.dto.InstagramProfileDto;
import site.petful.snsservice.instagram.profile.service.InstagramProfileService;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstagramInsightSyncTasklet implements Tasklet {

    private final InstagramTokenService tokenService;
    private final InstagramInsightsService insightService;
    private final InstagramProfileService profileService;
    private final InstagramTokenService instagramTokenService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception {
        log.info("==========Instagram 인사이트 동기화 시작==========");

        try {
            Long monthsToSync = chunkContext.getStepContext()
                .getJobParameters()
                .get("monthsToSync") != null ?
                (Long) chunkContext.getStepContext().getJobParameters().get("monthsToSync")
                : 1L;

            log.info("동기화할 개월 수: {}개월", monthsToSync);

            List<Long> users;
            Object targetUser = chunkContext.getStepContext().getJobParameters()
                .get("userNo");

            if (targetUser != null) {
                log.info("특정 사용자({})에 대해 인사이트 동기화 수행", targetUser);
                users = List.of((long) targetUser);
            } else {
                log.info("모든 사용자에 대해 인사이트 동기화 수행");
                users = instagramTokenService.getAllUserIds();
            }

            log.info("인사이트 동기화 대상 사용자 수: {}", users.size());

            for (Long userNo : users) {
                try {
                    String accessToken = tokenService.getAccessToken(userNo);
                    List<InstagramProfileDto> profiles = profileService.getProfiles(userNo);

                    for (InstagramProfileDto profile : profiles) {
                        try {
                            insightService.syncInsights(profile.id(), accessToken,
                                monthsToSync.intValue());
                            log.info("프로필 {} 인사이트 동기화 완료 ({}개월)", profile.id(), monthsToSync);
                        } catch (Exception e) {
                            log.error("프로필 {} 인사이트 동기화 실패: {}", profile.id(), e.getMessage());
                        }
                    }

                    log.info("사용자 {} 인사이트 동기화 완료", userNo);
                } catch (Exception e) {
                    log.error("사용자 {} 인사이트 동기화 실패: {}", userNo, e.getMessage());
                }
            }

            log.info("==========Instagram 인사이트 동기화 완료==========");
            return RepeatStatus.FINISHED;

        } catch (Exception e) {
            log.error("Instagram 인사이트 동기화 중 치명적 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}
