package site.petful.snsservice.instagram.service;

import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.snsservice.instagram.client.InstagramApiClient;
import site.petful.snsservice.instagram.client.dto.InstagramProfileResponse;
import site.petful.snsservice.instagram.client.dto.InstagramTokenResponse;
import site.petful.snsservice.instagram.domain.InstagramProfile;
import site.petful.snsservice.instagram.repository.InstagramProfileRepository;


// TODO [수정] AesEncryptService instagramApiClient로 옮기기
@Service
public class InstagramService {

    private final InstagramProfileRepository instagramProfileRepository;
    private final InstagramTokenService instagramTokenService;
    private final InstagramApiClient instagramApiClient;
    private final String clientId;
    private final String clientSecret;

    public InstagramService(
        InstagramTokenService instagramTokenService,
        InstagramApiClient instagramApiClient,
        InstagramProfileRepository instagramProfileRepository,
        @Value("${instagram.api.client_id}") String clientId,
        @Value("${instagram.api.client_secret}") String clientSecret
    ) {
        this.instagramProfileRepository = instagramProfileRepository;
        this.instagramTokenService = instagramTokenService;
        this.instagramApiClient = instagramApiClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Transactional
    public String connect(String token) {
        InstagramTokenResponse instagramTokenResponse = instagramApiClient.getLongLivedAccessToken(
            clientId, clientSecret
            , token);

        return instagramTokenService.saveToken(1L, instagramTokenResponse);
    }


    public List<InstagramProfileResponse> syncInstagramProfiles(Long userId) {
        // TODO [예외처리] null일때 오류 처리
        String accessToken = instagramTokenService.getDecryptedAccessToken(userId);
        String jsonString = instagramApiClient.fetchInstagramAccounts(accessToken);
        List<String> instagramIds = JsonPath.read(jsonString,
            "$.data[*].instagram_business_account.id");

        List<InstagramProfileResponse> profiles = new ArrayList<>();
        for (String instagramId : instagramIds) {
            System.out.println(instagramId);
            InstagramProfileResponse instagramProfileResponse = syncInstagramProfile(
                Long.parseLong(instagramId), accessToken);

            System.out.println(instagramProfileResponse);
            profiles.add(instagramProfileResponse);
        }

        return profiles;
    }


    public InstagramProfileResponse syncInstagramProfile(Long instagramId,
        String accessToken) {

        String fields = "username,name,profile_picture_url,biography,followers_count,follows_count,media_count,website";
        InstagramProfileResponse response = instagramApiClient.fetchInstagramProfile(instagramId,
            accessToken, fields);

        InstagramProfile profile = new InstagramProfile(response);

        profile = instagramProfileRepository.save(profile);

        return profile.toResponse();
    }
}
