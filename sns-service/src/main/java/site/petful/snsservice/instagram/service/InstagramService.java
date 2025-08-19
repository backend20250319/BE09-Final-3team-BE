package site.petful.snsservice.instagram.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.snsservice.instagram.client.InstagramApiClient;
import site.petful.snsservice.instagram.client.dto.InstagramTokenResponse;
import site.petful.snsservice.instagram.domain.InstagramToken;
import site.petful.snsservice.instagram.repository.InstagramRepository;

@Service
@RequiredArgsConstructor
public class InstagramService {

    private final AesEncryptService aesEncryptService;
    private final InstagramRepository instagramRepository;
    private final InstagramApiClient instagramApiClient; // WebClient 대신 ApiClient를 주입

    @Transactional
    public void connect(String token) {
        InstagramTokenResponse instagramTokenResponse = instagramApiClient.getLongLivedAccessToken(
            token).block();
        String encrypted = aesEncryptService.encrypt(instagramTokenResponse.getAccess_token());

        //TODO 여기 userId 수정
        InstagramToken instagramToken = new InstagramToken(1L, encrypted,
            instagramTokenResponse.getExpires_in());
        instagramRepository.save(instagramToken);

        System.out.println("[origin] " + instagramTokenResponse.getAccess_token());
        System.out.println("[encrypted] " + encrypted);
        System.out.println("[decrypted] " + aesEncryptService.decrypt(encrypted));
    }

    public List<String> getInstagramIds(Long userId) {
        String access_token = aesEncryptService.decrypt(
            instagramRepository.getByUserId(userId).getToken());

        List<String> ids = instagramApiClient.fetchInstagramIds(access_token).block();
        return ids;
    }
}
