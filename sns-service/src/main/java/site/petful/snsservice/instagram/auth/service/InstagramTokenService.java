package site.petful.snsservice.instagram.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.snsservice.instagram.auth.entity.InstagramTokenEntity;
import site.petful.snsservice.instagram.auth.repository.InstagramTokenRepository;
import site.petful.snsservice.instagram.client.dto.InstagramApiTokenResponseDto;
import site.petful.snsservice.util.AesEncryptService;

@Service
@RequiredArgsConstructor
public class InstagramTokenService {

    private final InstagramTokenRepository instagramTokenRepository;
    private final AesEncryptService aesEncryptService;

    @Transactional
    public String saveToken(Long userNo, InstagramApiTokenResponseDto longLivedAccessToken) {
        String encryptedToken = aesEncryptService.encrypt(longLivedAccessToken.access_token());
        // TODO: findByUserId로 기존 토큰이 있는지 확인

        InstagramTokenEntity token = new InstagramTokenEntity(userNo, encryptedToken,
            longLivedAccessToken.expires_in());
        token = instagramTokenRepository.save(token);

        return token.getToken();
    }

    public String getAccessToken(Long userNo) {
        InstagramTokenEntity token = instagramTokenRepository.findById(userNo)
            .orElseThrow(
                () -> new IllegalArgumentException("인스타그램 토큰을 찾을 수 없습니다. userId: " + userNo));
        return aesEncryptService.decrypt(token.getToken());
    }
}