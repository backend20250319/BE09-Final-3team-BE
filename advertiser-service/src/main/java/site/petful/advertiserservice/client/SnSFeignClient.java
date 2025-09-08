package site.petful.advertiserservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.dto.InstagramProfileDto;

import java.util.List;

@FeignClient(name="sns-service", path ="/instagram/profiles")
public interface SnSFeignClient {

    // 1. 사용자 instagram 프로필 조회
    @GetMapping("/advertiser/{userNo}")
    ApiResponse<List<InstagramProfileDto>> getProfileExternal(@PathVariable Long userNo);
}
