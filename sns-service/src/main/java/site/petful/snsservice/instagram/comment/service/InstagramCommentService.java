package site.petful.snsservice.instagram.comment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.snsservice.clova.service.ClovaApiService;
import site.petful.snsservice.instagram.client.InstagramApiClient;
import site.petful.snsservice.instagram.client.dto.InstagramApiCommentDto;
import site.petful.snsservice.instagram.client.dto.InstagramApiCommentResponseDto;
import site.petful.snsservice.instagram.comment.dto.CommentSearchCondition;
import site.petful.snsservice.instagram.comment.dto.CommentSentimentRatioResponseDto;
import site.petful.snsservice.instagram.comment.dto.InstagramCommentResponseDto;
import site.petful.snsservice.instagram.comment.dto.InstagramCommentStatusResponseDto;
import site.petful.snsservice.instagram.comment.entity.InstagramCommentEntity;
import site.petful.snsservice.instagram.comment.entity.Sentiment;
import site.petful.snsservice.instagram.comment.repository.InstagramCommentRepository;
import site.petful.snsservice.instagram.media.entity.InstagramMediaEntity;
import site.petful.snsservice.instagram.media.repository.InstagramMediaRepository;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;
import site.petful.snsservice.instagram.profile.repository.InstagramProfileRepository;

@Service
@RequiredArgsConstructor
public class InstagramCommentService {

    private static final String fields = "id,username,like_count,text,timestamp,replies";
    private final InstagramApiClient instagramApiClient;
    private final InstagramCommentRepository instagramCommentRepository;
    private final InstagramMediaRepository instagramMediaRepository;
    private final ClovaApiService clovaApiService;
    private final InstagramBannedWordService instagramBannedWordService;
    private final InstagramProfileRepository instagramProfileRepository;

    @Transactional
    public List<InstagramCommentResponseDto> syncInstagramCommentByMediaId(Long mediaId,
        String accessToken) {
        // 1. 필요한 부모 엔티티 조회
        InstagramMediaEntity media = instagramMediaRepository.findById(mediaId)
            .orElseThrow(() -> new IllegalArgumentException("조회된 게시글이 없습니다."));

        List<InstagramCommentEntity> finalSyncedEntities = new ArrayList<>();
        String after = null;

        do {
            InstagramApiCommentResponseDto response = instagramApiClient.fetchComments(
                mediaId, accessToken, fields, after, 25);

            List<InstagramApiCommentDto> pagedCommentsDto = response.getData();
            if (pagedCommentsDto == null || pagedCommentsDto.isEmpty()) {
                break;
            }

            List<InstagramCommentEntity> savedEntitiesOnPage = processCommentsPage(
                pagedCommentsDto, media, accessToken);
            finalSyncedEntities.addAll(savedEntitiesOnPage);

            after =
                response.getPaging() != null ? response.getPaging().getCursors().getAfter() : null;
        } while (after != null);

        return finalSyncedEntities.stream()
            .map(InstagramCommentResponseDto::fromEntity)
            .toList();
    }

    private List<InstagramCommentEntity> processCommentsPage(List<InstagramApiCommentDto> dtos,
        InstagramMediaEntity media, String accessToken) {

        Map<Long, InstagramCommentEntity> existingCommentsMap = findExistingComments(dtos);

        List<InstagramCommentEntity> entitiesToSave = new ArrayList<>();
        for (InstagramApiCommentDto dto : dtos) {
            InstagramCommentEntity existingComment = existingCommentsMap.get(dto.id());

            if (existingComment != null) {
                existingComment.update(dto);
                entitiesToSave.add(existingComment);
            } else {
                InstagramCommentEntity newComment = createNewCommentWithPolicy(dto, media,
                    accessToken);
                entitiesToSave.add(newComment);
            }
        }
        return instagramCommentRepository.saveAll(entitiesToSave);
    }

    private InstagramCommentEntity createNewCommentWithPolicy(InstagramApiCommentDto dto,
        InstagramMediaEntity media, String accessToken) {

        InstagramProfileEntity profile = media.getInstagramProfile();

        Sentiment sentiment = clovaApiService.analyzeSentiment(dto.text());
        Set<String> bannedWords = instagramBannedWordService.getBannedWords(profile);
        boolean isDeleted = false;

        boolean shouldDelete = profile.getAutoDelete()
            && (sentiment == Sentiment.NEGATIVE
            || bannedWords.stream().anyMatch(dto.text()::contains));

        if (shouldDelete) {
            instagramApiClient.deleteComment(dto.id(), accessToken);
            isDeleted = true;
        }

        return new InstagramCommentEntity(dto, sentiment, isDeleted, media, profile);
    }

    private Map<Long, InstagramCommentEntity> findExistingComments(
        List<InstagramApiCommentDto> dtos) {
        List<Long> commentIds = dtos.stream().map(InstagramApiCommentDto::id).toList();
        return instagramCommentRepository.findAllByIdIn(commentIds).stream()
            .collect(
                Collectors.toMap(InstagramCommentEntity::getId, entity -> entity));
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

    public InstagramCommentStatusResponseDto getCommentStatus(Long instagramId) {

        InstagramProfileEntity profile = instagramProfileRepository.findById(instagramId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 Instagram ID 입니다."));

        long totalComments = instagramCommentRepository.countByInstagramProfile(profile);
        long deletedComments = instagramCommentRepository.countByInstagramProfileAndIsDeleted(
            profile, true);
        long bannedWords = instagramBannedWordService.getBannedWords(profile).size();

        double deletionRate = totalComments == 0 ? 0.0
            : (double) deletedComments / totalComments * 100;

        return new InstagramCommentStatusResponseDto(totalComments, deletedComments,
            deletionRate, (long) bannedWords);
    }

    public Page<InstagramCommentResponseDto> searchComments(Long instagramId, Boolean isDeleted,
        Sentiment sentiment, String keyword, Pageable pageable) {

        CommentSearchCondition condition = new CommentSearchCondition();
        condition.setIsDeleted(isDeleted);
        condition.setSentiment(sentiment);
        condition.setKeyword(keyword);

        Page<InstagramCommentEntity> entityPage = instagramCommentRepository.searchComments(
            instagramId,
            condition, pageable);

        // Page<Entity>를 Page<Dto>로 변환
        return entityPage.map(InstagramCommentResponseDto::fromEntity);
    }

    public CommentSentimentRatioResponseDto getSentimentRatio(Long instagramId) {

        InstagramProfileEntity profile = instagramProfileRepository.findById(instagramId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 Instagram ID 입니다."));

        List<InstagramCommentEntity> comments = instagramCommentRepository.findInstagramCommentEntitiesByInstagramProfile(
            profile);

        long totalCount = comments.size();
        if (totalCount == 0) {
            return new CommentSentimentRatioResponseDto(0, 0, 0);
        }

        long positiveCount = 0;
        long neutralCount = 0;
        long negativeCount = 0;
        for (InstagramCommentEntity comment : comments) {
            switch (comment.getSentiment()) {
                case POSITIVE -> positiveCount++;
                case NEUTRAL -> neutralCount++;
                case NEGATIVE -> negativeCount++;
            }
        }

        double positiveRatio = (double) positiveCount / totalCount * 100;
        double neutralRatio = (double) neutralCount / totalCount * 100;
        double negativeRatio = (double) negativeCount / totalCount * 100;

        return new CommentSentimentRatioResponseDto(positiveRatio, negativeRatio, neutralRatio);
    }
}
