package site.petful.snsservice.instagram.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.snsservice.instagram.client.InstagramApiClient;
import site.petful.snsservice.instagram.client.dto.InstagramApiTokenResponseDto;

@Service
public class InstagramAuthService {

    private final InstagramTokenService instagramTokenService;
    private final InstagramApiClient instagramApiClient;
    private final String clientId;
    private final String clientSecret;

    public InstagramAuthService(
        InstagramTokenService instagramTokenService,
        InstagramApiClient instagramApiClient,
        @Value("${instagram.api.client_id}") String clientId,
        @Value("${instagram.api.client_secret}") String clientSecret) {
        this.instagramTokenService = instagramTokenService;
        this.instagramApiClient = instagramApiClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Transactional
    public String connect(Long userNo, String accessToken) {
        InstagramApiTokenResponseDto instagramApiTokenResponseDto = instagramApiClient.getLongLivedAccessToken(
            clientId, clientSecret, accessToken);

        //TODO에 데이터 가져오는 거 어떻게 할지
        return instagramTokenService.saveToken(userNo, instagramApiTokenResponseDto);
    }
}
