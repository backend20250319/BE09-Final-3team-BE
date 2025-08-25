package site.petful.snsservice.instagram.comment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.snsservice.clova.service.ClovaApiService;
import site.petful.snsservice.instagram.client.InstagramApiClient;
import site.petful.snsservice.instagram.client.dto.InstagramApiCommentDto;
import site.petful.snsservice.instagram.client.dto.InstagramApiCommentResponseDto;
import site.petful.snsservice.instagram.comment.dto.InstagramCommentResponseDto;
import site.petful.snsservice.instagram.comment.entity.InstagramCommentEntity;
import site.petful.snsservice.instagram.comment.entity.Sentiment;
import site.petful.snsservice.instagram.comment.repository.InstagramCommentRepository;
import site.petful.snsservice.instagram.media.entity.InstagramMediaEntity;
import site.petful.snsservice.instagram.media.repository.InstagramMediaRepository;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;

@Service
@RequiredArgsConstructor
public class InstagramCommentService {

    private static final String fields = "id,username,like_count,text,timestamp,replies";
    private final InstagramApiClient instagramApiClient;
    private final InstagramCommentRepository instagramCommentRepository;
    private final InstagramMediaRepository instagramMediaRepository;
    private final ClovaApiService clovaApiService;
    private final InstagramBannedWordService instagramBannedWordService;


    @Transactional
    public List<InstagramCommentResponseDto> syncInstagramCommentByMediaId(Long mediaId,
        String accessToken) {

        InstagramMediaEntity media = instagramMediaRepository.findById(mediaId)
            .orElseThrow(() -> new IllegalArgumentException("조회된 게시글이 없습니다."));
        InstagramProfileEntity profile = media.getInstagramProfile();

        List<InstagramCommentEntity> finalSyncedEntities = new ArrayList<>();
        String after = null;

        do {
            InstagramApiCommentResponseDto response = instagramApiClient.fetchComments(
                mediaId, accessToken, fields, after, 25);

            List<InstagramApiCommentDto> pagedCommentsDto = response.getData();
            if (pagedCommentsDto == null || pagedCommentsDto.isEmpty()) {
                break;
            }

            List<Long> commentIds = pagedCommentsDto.stream()
                .map(InstagramApiCommentDto::id)
                .toList();
            Map<Long, InstagramCommentEntity> existingCommentsMap = instagramCommentRepository.findAllById(
                    commentIds)
                .stream()
                .collect(Collectors.toMap(InstagramCommentEntity::getId, entity -> entity));

            List<InstagramCommentEntity> entitiesToSave = new ArrayList<>();
            for (InstagramApiCommentDto dto : pagedCommentsDto) {
                InstagramCommentEntity existingComment = existingCommentsMap.get(dto.id());

                if (existingComment != null) {
                    existingComment.update(dto);
                    entitiesToSave.add(existingComment);
                } else {

                    Sentiment sentiment = clovaApiService.analyzeSentiment(dto.text());
                    Set<String> BannedWords = instagramBannedWordService.getBannedWords(profile);
                    boolean isDeleted = false;

                    if (profile.getAutoDelete() && (sentiment == Sentiment.NEGATIVE
                        && BannedWords.contains(dto.text()))) {
                        instagramApiClient.deleteComment(dto.id(), accessToken);
                        isDeleted = true;
                    }

                    entitiesToSave.add(
                        new InstagramCommentEntity(dto, sentiment,
                            isDeleted, media,
                            profile));
                }
            }

            finalSyncedEntities.addAll(instagramCommentRepository.saveAll(entitiesToSave));

            after =
                response.getPaging() != null ? response.getPaging().getCursors().getAfter() : null;
        } while (after != null);

        return finalSyncedEntities.stream()
            .map(InstagramCommentResponseDto::fromEntity)
            .toList();
    }

    public List<InstagramCommentResponseDto> getComments(Long mediaId) {
        List<InstagramCommentResponseDto> comments = instagramCommentRepository.findById(
                mediaId)
            .stream()
            .map(InstagramCommentResponseDto::fromEntity)
            .toList();

        return comments;
    }

    @Transactional
    public void deleteComment(Long commentId, String accessToken) {
        InstagramCommentEntity entity = instagramCommentRepository.findById(commentId)
            .orElseThrow(() -> new NoSuchElementException("삭제할 댓글이 없습니다."));

        entity.delete();
        instagramApiClient.deleteComment(commentId, accessToken);

        instagramCommentRepository.save(entity);
    }


}
