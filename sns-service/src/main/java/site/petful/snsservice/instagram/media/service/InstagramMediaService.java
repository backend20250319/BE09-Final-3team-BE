package site.petful.snsservice.instagram.media.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import site.petful.snsservice.instagram.auth.service.InstagramTokenService;
import site.petful.snsservice.instagram.client.InstagramApiClient;
import site.petful.snsservice.instagram.client.dto.InstagramMediaResponseDto;
import site.petful.snsservice.instagram.media.dto.InstagramMediaDto;
import site.petful.snsservice.instagram.media.entity.InstagramMediaEntity;
import site.petful.snsservice.instagram.media.repository.InstagramMediaRepository;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;
import site.petful.snsservice.instagram.profile.repository.InstagramProfileRepository;

@Service
public class InstagramMediaService {

    private final InstagramTokenService instagramTokenService;
    private final InstagramApiClient instagramApiClient;
    private final InstagramMediaRepository instagramMediaRepository;
    private final InstagramProfileRepository instagramProfileRepository;

    public InstagramMediaService(
        InstagramTokenService instagramTokenService,
        InstagramApiClient instagramApiClient,
        InstagramMediaRepository instagramMediaRepository,
        InstagramProfileRepository instagramProfileRepository) {
        this.instagramTokenService = instagramTokenService;
        this.instagramApiClient = instagramApiClient;
        this.instagramMediaRepository = instagramMediaRepository;
        this.instagramProfileRepository = instagramProfileRepository;
    }

    public List<InstagramMediaDto> syncInstagramMedia(Long userId, Long instagramId) {
        String accessToken = instagramTokenService.getAccessTokenByUserId(userId);
        String fields = "id,caption,media_type,media_url,thumbnail_url,permalink,timestamp,is_comment_enabled,like_count,comments_count";

        List<InstagramMediaDto> allMediaDto = new ArrayList<>();
        String after = null;
        do {

            InstagramMediaResponseDto response = instagramApiClient.fetchInstagramMedia(
                instagramId,
                accessToken, fields, after, 25, null);

            allMediaDto.addAll(response.getData());

            after =
                response.getPaging() != null ? response.getPaging().getCursors().getAfter() : null;
        } while (after != null);

        InstagramProfileEntity instagramProfile = instagramProfileRepository.findById(instagramId)
            .orElseThrow(() -> new IllegalArgumentException("조회된 인스타 그램 프로필이 없습니다."));

        List<InstagramMediaEntity> entities = allMediaDto.stream()
            .map((dto) -> new InstagramMediaEntity(dto, instagramProfile))
            .toList();

        entities = instagramMediaRepository.saveAll(entities);

        allMediaDto = entities.stream()
            .map(InstagramMediaEntity::toDto)
            .toList();

        return allMediaDto;
    }
}
