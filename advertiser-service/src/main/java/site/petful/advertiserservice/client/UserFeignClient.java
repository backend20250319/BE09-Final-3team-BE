package site.petful.advertiserservice.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.config.FeignConfig;
import site.petful.advertiserservice.dto.ProfileResponse;
import site.petful.advertiserservice.dto.ReportRequest;

@FeignClient(name = "user-service", path = "/auth", configuration= FeignConfig.class)
public interface UserFeignClient {

    // 1. 사용자 프로필 조회
    @GetMapping("/profile/{userNo}")
    ApiResponse<ProfileResponse> getProfile(@PathVariable String userNo);

    // 2. 사용자 신고하기
    @PostMapping("/reports/advertiser")
    ApiResponse<String> reportUserByAdvertiser(
            @RequestHeader("USER-NO") Long advertiserNo,
            @Valid @RequestBody ReportRequest request);
}