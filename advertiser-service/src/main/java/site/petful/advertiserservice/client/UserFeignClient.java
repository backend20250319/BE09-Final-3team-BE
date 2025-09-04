package site.petful.advertiserservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.config.FeignConfig;
import site.petful.advertiserservice.dto.ProfileResponse;

@FeignClient(name = "user-service", path = "/auth", configuration= FeignConfig.class)
public interface UserFeignClient {

    // 1. 사용자 프로필 조회
    @GetMapping("/profile/{userNo}")
    ApiResponse<ProfileResponse> getProfile(@PathVariable String userNo);
}