package site.petful.snsservice.instagram.media.service;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.snsservice.instagram.client.InstagramApiClient;
import site.petful.snsservice.instagram.client.dto.InstagramApiMediaResponseDto;
import site.petful.snsservice.instagram.media.dto.InstagramAnalysisMediasResponseDto;
import site.petful.snsservice.instagram.media.dto.InstagramMediaDto;
import site.petful.snsservice.instagram.media.entity.InstagramMediaEntity;
import site.petful.snsservice.instagram.media.repository.InstagramMediaRepository;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;
import site.petful.snsservice.instagram.profile.repository.InstagramProfileRepository;

@Service
@RequiredArgsConstructor
public class InstagramMediaService {

    private static final String fields = "id,caption,media_type,media_url,thumbnail_url,permalink,timestamp,is_comment_enabled,like_count,comments_count";

    private final InstagramApiClient instagramApiClient;
    private final InstagramMediaRepository instagramMediaRepository;
    private final InstagramProfileRepository instagramProfileRepository;


    @Transactional
    public List<InstagramMediaDto> syncInstagramMedia(Long instagramId, String accessToken) {

        InstagramProfileEntity instagramProfile = instagramProfileRepository.findById(
                instagramId)
            .orElseThrow(() -> new IllegalArgumentException("조회된 인스타 그램 프로필이 없습니다."));
        List<InstagramMediaEntity> finalResultEntities = new ArrayList<>();
        String after = null;

        do {
            InstagramApiMediaResponseDto response = instagramApiClient.fetchMedia(
                instagramId, accessToken, fields, after, 25, null);

            List<InstagramMediaDto> pagedDtos = response.getData();
            if (pagedDtos.isEmpty()) {
                break;
            }

            List<Long> mediaIds = pagedDtos.stream().map(InstagramMediaDto::id).toList();
            Map<Long, InstagramMediaEntity> existingMediaMap =
                instagramMediaRepository.findAllByIdIn(mediaIds).stream()
                    .collect(Collectors.toMap(InstagramMediaEntity::getId, entity -> entity));

            List<InstagramMediaEntity> entitiesToSave = new ArrayList<>();
            for (InstagramMediaDto dto : pagedDtos) {
                InstagramMediaEntity entity = existingMediaMap.get(dto.id());
                if (entity != null) {
                    entity.update(dto);
                    entitiesToSave.add(entity);
                } else {
                    entitiesToSave.add(new InstagramMediaEntity(dto, instagramProfile));
                }
            }

            finalResultEntities.addAll(instagramMediaRepository.saveAll(entitiesToSave));

            after =
                response.getPaging() != null ? response.getPaging().getCursors().getAfter() : null;

        } while (after != null);

        return finalResultEntities.stream()
            .map(InstagramMediaEntity::toDto)
            .toList();
    }

    public List<InstagramMediaDto> getMedias(@NotNull Long instagramId) {
        InstagramProfileEntity profileEntity = instagramProfileRepository.findById(instagramId)
            .orElseThrow(() ->
                new NotFoundException("해당 인스타그램 ID를 찾을 수 없습니다."));

        List<InstagramMediaEntity> mediaEntities = instagramMediaRepository.findAllByInstagramProfile(
            profileEntity);

        return mediaEntities.stream().map(InstagramMediaEntity::toDto).toList();
    }

    public InstagramMediaDto getMedia(@NotNull Long mediaId) {

        InstagramMediaEntity mediaEntity = instagramMediaRepository.findById(mediaId)
            .orElseThrow(() ->
                new NotFoundException("해당 미디어 ID를 찾을 수 없습니다."));
        return mediaEntity.toDto();
    }

    public List<InstagramMediaDto> getTopMedias(@NotNull Long instagramId) {

        InstagramProfileEntity profileEntity = instagramProfileRepository.findById(instagramId)
            .orElseThrow(() ->
                new NotFoundException("해당 인스타그램 ID를 찾을 수 없습니다."));

        List<InstagramMediaEntity> topMediaEntities = instagramMediaRepository
            .findTop3ByInstagramProfileOrderByLikeCountDesc(profileEntity);

        return topMediaEntities.stream().map(InstagramMediaEntity::toDto).toList();
    }

    public InstagramAnalysisMediasResponseDto analyzeMedias(@NotNull Long instagramId) {

        InstagramProfileEntity profileEntity = instagramProfileRepository.findById(instagramId)
            .orElseThrow(() ->
                new NotFoundException("해당 인스타그램 ID를 찾을 수 없습니다."));

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime thirtyDaysAgo = now.minusDays(30);
        OffsetDateTime sixtyDaysAgo = now.minusDays(60);

        List<InstagramMediaEntity> recent30 = instagramMediaRepository
            .findAllByInstagramProfileAndTimestampBetween(profileEntity, thirtyDaysAgo, now);

        List<InstagramMediaEntity> prev30 = instagramMediaRepository
            .findAllByInstagramProfileAndTimestampBetween(profileEntity, sixtyDaysAgo,
                thirtyDaysAgo);

        if (recent30.isEmpty() && prev30.isEmpty()) {
            return new InstagramAnalysisMediasResponseDto(0.0, 0.0, 0L, 0L, 0.0, 0.0, 0.0, 0.0);
        }

        double avgLikesRecent = calculateAverageLikes(recent30);
        double avgCommentsRecent = calculateAverageComments(recent30);
        long topLikeRecent = calculateTopLike(recent30);
        long topCommentRecent = calculateTopComment(recent30);

        double avgLikesPrev = calculateAverageLikes(prev30);
        double avgCommentsPrev = calculateAverageComments(prev30);
        long topLikePrev = calculateTopLike(prev30);
        long topCommentPrev = calculateTopComment(prev30);

        double likesMoM = calculateMomPercent(avgLikesPrev, avgLikesRecent);
        double commentsMoM = calculateMomPercent(avgCommentsPrev, avgCommentsRecent);
        double topLikeMoM = calculateMomPercent((double) topLikePrev, (double) topLikeRecent);
        double topCommentMoM = calculateMomPercent((double) topCommentPrev,
            (double) topCommentRecent);

        return new InstagramAnalysisMediasResponseDto(
            avgLikesRecent, avgCommentsRecent, topLikeRecent, topCommentRecent,
            likesMoM, commentsMoM, topLikeMoM, topCommentMoM
        );
    }

    private double calculateAverageLikes(List<InstagramMediaEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (InstagramMediaEntity entity : entities) {
            sum += entity.getLikeCount();
        }
        return sum / entities.size();
    }

    private double calculateAverageComments(List<InstagramMediaEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (InstagramMediaEntity entity : entities) {
            sum += entity.getCommentsCount();
        }
        return sum / entities.size();
    }

    private long calculateTopLike(List<InstagramMediaEntity> entities) {
        long top = 0L;
        for (InstagramMediaEntity entity : entities) {
            top = Math.max(top, entity.getLikeCount());
        }
        return top;
    }

    private long calculateTopComment(List<InstagramMediaEntity> entities) {
        long top = 0L;
        for (InstagramMediaEntity entity : entities) {
            top = Math.max(top, entity.getCommentsCount());
        }
        return top;
    }

    private double calculateMomPercent(double previous, double current) {
        if (previous == 0.0) {
            return current == 0.0 ? 0.0 : 100.0;
        }
        return ((current - previous) / previous) * 100.0;
    }
}
