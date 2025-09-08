package site.petful.advertiserservice.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.advertiserservice.client.UserFeignClient;
import site.petful.advertiserservice.dto.ProfileResponse;
import site.petful.advertiserservice.dto.ReportRequest;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserFeignClient userFeignClient;

    // 1. 사용자 프로필 조회
    public ProfileResponse getProfile(String userNo) {
        return userFeignClient.getProfile(userNo).getData();
    }

    // 2. 사용자 신고하기
    public void reportUser(Long advertiserNo, @Valid ReportRequest request) {
        userFeignClient.reportUserByAdvertiser(advertiserNo, request);
    }
}
