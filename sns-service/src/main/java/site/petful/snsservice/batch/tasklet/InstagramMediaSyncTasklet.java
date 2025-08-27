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
import site.petful.snsservice.instagram.media.service.InstagramMediaService;
import site.petful.snsservice.instagram.profile.dto.InstagramProfileDto;
import site.petful.snsservice.instagram.profile.service.InstagramProfileService;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstagramMediaSyncTasklet implements Tasklet {

    private final InstagramTokenService tokenService;
    private final InstagramMediaService mediaService;
    private final InstagramProfileService profileService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("==========Instagram 미디어 데이터 동기화 시작==========");

        try {
            List<Long> users;
            Object targetUser = chunkContext.getStepContext().getJobParameters()
                .get("userNo");

            if (targetUser != null) {
                log.info("특정 사용자({})에 대해 인사이트 동기화 수행", targetUser);
                users = List.of((long) targetUser);
            } else {
                log.info("모든 사용자에 대해 인사이트 동기화 수행");
                //Todo: 실제 사용자 목록을 DB에서 가져오도록 수정 필요
                users = List.of(1L);
            }

            log.info("프로필 동기화 대상 사용자 수: {}", users.size());

            for (Long userNo : users) {
                try {
                    String accessToken = tokenService.getAccessToken(userNo);
                    List<InstagramProfileDto> profiles = profileService.getProfiles(userNo);

                    for (InstagramProfileDto profile : profiles) {
                        mediaService.syncInstagramMedia(profile.id(), accessToken);
                    }
                    log.info("사용자 {} 미디어 데이터 동기화 완료", userNo);
                } catch (Exception e) {
                    log.error("사용자 {} 미디어 데이터 동기화 실패: {}", userNo, e.getMessage());
                }
            }

            log.info("==========Instagram 미디어 데이터 동기화 완료==========");
            return RepeatStatus.FINISHED;

        } catch (Exception e) {
            log.error("Instagram 미디어 데이터 동기화 중 치명적 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}
