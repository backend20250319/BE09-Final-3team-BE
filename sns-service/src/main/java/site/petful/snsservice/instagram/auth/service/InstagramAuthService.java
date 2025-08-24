package site.petful.snsservice.instagram.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.snsservice.instagram.client.InstagramApiClient;
import site.petful.snsservice.instagram.client.dto.InstagramTokenResponseDto;

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
    public String connect(String token) {
        InstagramTokenResponseDto instagramTokenResponseDto = instagramApiClient.getLongLivedAccessToken(
            clientId, clientSecret, token);

        return instagramTokenService.saveToken(1L, instagramTokenResponseDto);
    }
}
