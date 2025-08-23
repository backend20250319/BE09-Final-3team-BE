package site.petful.snsservice.instagram.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import site.petful.snsservice.instagram.client.InstagramApiClient;
import site.petful.snsservice.instagram.client.dto.InstagramCommentResponseDto;
import site.petful.snsservice.instagram.domain.InstagramCommentEntity;
import site.petful.snsservice.instagram.domain.InstagramMediaEntity;
import site.petful.snsservice.instagram.dto.InstagramCommentDto;
import site.petful.snsservice.instagram.repository.InstagramCommentRepository;
import site.petful.snsservice.instagram.repository.InstagramMediaRepository;

@Service
public class InstagramCommentService {

    private final InstagramTokenService instagramTokenService;
    private final InstagramApiClient instagramApiClient;
    private final InstagramCommentRepository instagramCommentRepository;
    private final InstagramMediaRepository instagramMediaRepository;

    public InstagramCommentService(
        InstagramTokenService instagramTokenService,
        InstagramApiClient instagramApiClient,
        InstagramCommentRepository instagramCommentRepository,
        InstagramMediaRepository instagramMediaRepository) {
        this.instagramTokenService = instagramTokenService;
        this.instagramApiClient = instagramApiClient;
        this.instagramCommentRepository = instagramCommentRepository;
        this.instagramMediaRepository = instagramMediaRepository;
    }

    public List<InstagramCommentDto> syncInstagramCommentByUserIdAndMediaId(Long userId,
        Long mediaId) {

        String accessToken = instagramTokenService.getAccessTokenByUserId(userId);

        String fields = "id,username,like_count,text,timestamp,replies";

        List<InstagramCommentDto> allCommentsDto = new ArrayList<>();

        String after = null;
        do {

            InstagramCommentResponseDto response = instagramApiClient.fetchInstagramComments(
                mediaId, accessToken, fields, after, 25);

            allCommentsDto.addAll(response.getData());

            after =
                response.getPaging() != null ? response.getPaging().getCursors().getAfter() : null;
        } while (after != null);

        InstagramMediaEntity media = instagramMediaRepository.findById(mediaId)
            .orElseThrow(() -> new IllegalArgumentException("조회된 게시글이 없습니다."));

        List<InstagramCommentEntity> entities = allCommentsDto.stream()
            .map((dto) -> new InstagramCommentEntity(dto, media))
            .toList();

        entities = instagramCommentRepository.saveAll(entities);

        allCommentsDto = entities.stream()
            .map(InstagramCommentEntity::toDto)
            .toList();

        return allCommentsDto;
    }
}
