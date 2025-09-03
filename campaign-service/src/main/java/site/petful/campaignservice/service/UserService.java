package site.petful.campaignservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.campaignservice.client.UserFeignClient;
import site.petful.campaignservice.dto.ProfileResponse;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserFeignClient userFeignClient;

    // 1. 사용자 프로필 조회
    public ProfileResponse getProfile(Long userNo) {
        return userFeignClient.getProfile().getData();
    }
}
