package site.petful.campaignservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.dto.ProfileResponse;

@FeignClient(name = "user-service", path = "/auth")
public interface UserFeignClient {

    // 1. 사용자 프로필 조회
    @GetMapping("/profile")
    ApiResponse<ProfileResponse> getProfile();
}
