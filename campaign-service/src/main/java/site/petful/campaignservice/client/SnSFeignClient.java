package site.petful.campaignservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.dto.InstagramProfileDto;

import java.util.List;

@FeignClient(name="sns-service", path ="/instagram/profiles")
public interface SnSFeignClient {

    // 1. 사용자 instagram 프로필 조회
    @GetMapping
    ApiResponse<List<InstagramProfileDto>> getProfiles();
}
