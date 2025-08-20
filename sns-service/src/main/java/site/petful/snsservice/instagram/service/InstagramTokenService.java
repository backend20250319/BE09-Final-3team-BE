package site.petful.snsservice.instagram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.snsservice.instagram.client.dto.InstagramTokenResponse;
import site.petful.snsservice.instagram.domain.InstagramToken;
import site.petful.snsservice.instagram.repository.InstagramTokenRepository;

@Service
@RequiredArgsConstructor
public class InstagramTokenService {

    private final InstagramTokenRepository instagramTokenRepository;
    private final AesEncryptService aesEncryptService;

    @Transactional
    public String saveToken(Long userId, InstagramTokenResponse instagramTokenResponse) {
        String encryptedToken = aesEncryptService.encrypt(instagramTokenResponse.access_token());
        // TODO: findByUserId로 기존 토큰이 있는지 확인
        InstagramToken token = new InstagramToken(userId, encryptedToken,
            instagramTokenResponse.expires_in());
        token = instagramTokenRepository.save(token);

        return token.getToken();
    }

    public String getDecryptedAccessToken(Long userId) {
        // TODO [예외처리] Optional을 사용하여 null일 때 예외를 던지는 것이 안전합니다.
        InstagramToken token = instagramTokenRepository.findByUserId(userId)
            .orElseThrow(
                () -> new IllegalArgumentException("인스타그램 토큰을 찾을 수 없습니다. userId: " + userId));
        return aesEncryptService.decrypt(token.getToken());
    }
}