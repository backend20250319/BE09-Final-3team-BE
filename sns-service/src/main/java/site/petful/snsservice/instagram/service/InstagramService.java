package site.petful.snsservice.instagram.service;

import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.snsservice.instagram.client.InstagramApiClient;
import site.petful.snsservice.instagram.client.dto.InstagramMediaResponseDto;
import site.petful.snsservice.instagram.client.dto.InstagramProfileResponseDto;
import site.petful.snsservice.instagram.client.dto.InstagramTokenResponseDto;
import site.petful.snsservice.instagram.domain.InstagramMediaEntity;
import site.petful.snsservice.instagram.domain.InstagramProfileEntity;
import site.petful.snsservice.instagram.dto.InstagramMediaDto;
import site.petful.snsservice.instagram.repository.InstagramMediaRepository;
import site.petful.snsservice.instagram.repository.InstagramProfileRepository;


// TODO [수정] AesEncryptService instagramApiClient로 옮기기
@Service
public class InstagramService {

    private final InstagramProfileRepository instagramProfileRepository;
    private final InstagramTokenService instagramTokenService;
    private final InstagramApiClient instagramApiClient;
    private final String clientId;
    private final String clientSecret;
    private final InstagramMediaRepository instagramMediaRepository;

    public InstagramService(
        InstagramTokenService instagramTokenService,
        InstagramApiClient instagramApiClient,
        InstagramProfileRepository instagramProfileRepository,
        @Value("${instagram.api.client_id}") String clientId,
        @Value("${instagram.api.client_secret}") String clientSecret,
        InstagramMediaRepository instagramMediaRepository) {
        this.instagramProfileRepository = instagramProfileRepository;
        this.instagramTokenService = instagramTokenService;
        this.instagramApiClient = instagramApiClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.instagramMediaRepository = instagramMediaRepository;
    }

    @Transactional
    public String connect(String token) {
        InstagramTokenResponseDto instagramTokenResponseDto = instagramApiClient.getLongLivedAccessToken(
            clientId, clientSecret
            , token);

        return instagramTokenService.saveToken(1L, instagramTokenResponseDto);
    }


    public List<InstagramProfileResponseDto> syncInstagramProfiles(Long userId) {
        // TODO [예외처리] null일때 오류 처리
        String accessToken = instagramTokenService.getDecryptedAccessToken(userId);
        String jsonString = instagramApiClient.fetchInstagramAccounts(accessToken);
        List<String> instagramIds = JsonPath.read(jsonString,
            "$.data[*].instagram_business_account.id");

        List<InstagramProfileResponseDto> profiles = new ArrayList<>();
        for (String instagramId : instagramIds) {
            System.out.println(instagramId);
            InstagramProfileResponseDto instagramProfileResponseDto = syncInstagramProfile(
                Long.parseLong(instagramId), accessToken, userId);

            profiles.add(instagramProfileResponseDto);
        }

        return profiles;
    }


    public InstagramProfileResponseDto syncInstagramProfile(Long instagramId,
        String accessToken, Long userId) {

        // TODO [저장 처리] 저장을 위에서 한번에 할지 아니면 개별적으로 할지 고민
        String fields = "username,name,profile_picture_url,biography,followers_count,follows_count,media_count,website";
        InstagramProfileResponseDto response = instagramApiClient.fetchInstagramProfile(instagramId,
            accessToken, fields);

        InstagramProfileEntity profile = new InstagramProfileEntity(response, userId);

        profile = instagramProfileRepository.save(profile);

        return profile.toInstagramProfileDto();
    }

    public List<InstagramMediaDto> syncInstagramMedia(Long userId, Long instagramId) {
        String accessToken = instagramTokenService.getDecryptedAccessToken(userId);
        String fields = "id,caption,media_type,media_url,thumbnail_url,permalink,timestamp,is_comment_enabled,like_count,comments_count";

        List<InstagramMediaDto> allMediaDto = new ArrayList<>();
        String after = null;
        do {

            InstagramMediaResponseDto response = instagramApiClient.fetchInstagramMedia(
                instagramId,
                accessToken, fields, after, 25);

            for (InstagramMediaDto media : response.getData()) {
                allMediaDto.add(media);
            }

            after =
                response.getPaging() != null ? response.getPaging().getCursors().getAfter() : null;
        } while (after != null);

        System.out.println(allMediaDto);

        // TODO [저장 처리]
        InstagramProfileEntity instagramProfile = instagramProfileRepository.findById(instagramId)
            .orElseThrow(() -> new IllegalArgumentException("Instagram profile not found"));

        List<InstagramMediaEntity> entities = allMediaDto.stream()
            .map((dto) -> new InstagramMediaEntity(dto, instagramProfile))
            .toList();

        entities = instagramMediaRepository.saveAll(entities);

        allMediaDto = entities.stream()
            .map(InstagramMediaEntity::toInstagramMediaDto)
            .toList();

        return allMediaDto;
    }
}
