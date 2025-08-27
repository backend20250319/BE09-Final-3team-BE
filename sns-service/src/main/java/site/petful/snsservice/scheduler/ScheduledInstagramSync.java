package site.petful.snsservice.scheduler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.petful.snsservice.instagram.auth.service.InstagramTokenService;
import site.petful.snsservice.instagram.comment.service.InstagramCommentService;
import site.petful.snsservice.instagram.insight.service.InstagramInsightsService;
import site.petful.snsservice.instagram.media.dto.InstagramMediaDto;
import site.petful.snsservice.instagram.media.service.InstagramMediaService;
import site.petful.snsservice.instagram.profile.dto.InstagramProfileDto;
import site.petful.snsservice.instagram.profile.service.InstagramProfileService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledInstagramSync {

    private final InstagramTokenService tokenService;
    private final InstagramProfileService profileService;
    private final InstagramInsightsService insightService;
    private final InstagramMediaService mediaService;
    private final InstagramCommentService commentService;

    // 매일 새벽 2시 실행
//    @Scheduled(cron = "0 0 2 * * *")
    public void runSync() {
        log.info("Instagram 게시물 + 댓글 동기화 시작");
        // TODO userNo 가져오기 필요
        List<Long> users = List.of(1L);

        for (Long userNo : users) {
            String accessToken = tokenService.getAccessToken(userNo);

            profileService.syncAllInstagramProfiles(userNo, accessToken);
            List<InstagramProfileDto> profiles = profileService.getProfiles(userNo);

            for (InstagramProfileDto profile : profiles) {
                insightService.syncInsights(profile.id(), accessToken, 1);
                mediaService.syncInstagramMedia(userNo, accessToken);

                mediaService.getMedias(profile.id()).stream()
                    .map(InstagramMediaDto::id)
                    .forEach(mediaId -> {
                        commentService.syncInstagramCommentByMediaId(mediaId, accessToken);
                    });
            }
        }
        log.info("Instagram 동기화 완료");
    }
}
