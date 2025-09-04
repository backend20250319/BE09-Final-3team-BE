package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.advertiserservice.client.UserFeignClient;
import site.petful.advertiserservice.dto.ProfileResponse;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserFeignClient userFeignClient;

    // 1. 사용자 프로필 조회
    public ProfileResponse getProfile(String userNo) {
        return userFeignClient.getProfile(userNo).getData();
    }
}
