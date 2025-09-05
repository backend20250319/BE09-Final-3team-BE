package site.petful.snsservice.instagram.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.snsservice.instagram.client.InstagramApiClient;
import site.petful.snsservice.instagram.client.dto.InstagramApiTokenResponseDto;

@Slf4j
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
        log.info("Instagram 연결 시도 - userNo: {}", userNo);
        log.info("Instagram 연결 시도 - accessToken: {}", accessToken);
        InstagramApiTokenResponseDto instagramApiTokenResponseDto = instagramApiClient.getLongLivedAccessToken(
            clientId, clientSecret, accessToken);

        return instagramTokenService.saveToken(userNo, instagramApiTokenResponseDto);
    }
}
