package site.petful.petservice.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import site.petful.petservice.common.ApiResponse;
import site.petful.petservice.config.FeignAuthConfig;
import site.petful.petservice.dto.SimpleProfileResponse;


@FeignClient(name = "user-service", path = "/auth/profile",configuration = FeignAuthConfig.class)
public interface UserClient {

    @GetMapping("/api/v1/user-service/auth/profile/{userNo}")
    UserProfileResponse getUserProfie(@PathVariable Long userNo);

    @GetMapping("/simple")
    ApiResponse<SimpleProfileResponse> getUserBrief(@RequestParam("userNo") Long userNo);
}
