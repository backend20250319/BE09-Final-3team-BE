package site.petful.snsservice.instagram.service;

import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import site.petful.snsservice.instagram.client.InstagramApiClient;
import site.petful.snsservice.instagram.client.dto.InstagramProfileResponseDto;
import site.petful.snsservice.instagram.domain.InstagramProfileEntity;
import site.petful.snsservice.instagram.repository.InstagramProfileRepository;

@Service
public class InstagramProfileService {

    private final InstagramProfileRepository instagramProfileRepository;
    private final InstagramTokenService instagramTokenService;
    private final InstagramApiClient instagramApiClient;

    public InstagramProfileService(
        InstagramProfileRepository instagramProfileRepository,
        InstagramTokenService instagramTokenService,
        InstagramApiClient instagramApiClient) {
        this.instagramProfileRepository = instagramProfileRepository;
        this.instagramTokenService = instagramTokenService;
        this.instagramApiClient = instagramApiClient;
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
}
