package site.petful.advertiserservice.client;

import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.dto.InstagramProfileDto;
import site.petful.advertiserservice.dto.campaign.InstagramMediaDto;

import java.util.List;

@FeignClient(name="sns-service", path ="/instagram")
public interface SnSFeignClient {

    // 1. 사용자 instagram 프로필 조회
    @GetMapping("/profiles/advertiser/{userNo}")
    ApiResponse<List<InstagramProfileDto>> getProfileExternal(@PathVariable Long userNo);

    // 2. instagramId별 Media 조회
    @GetMapping("/medias")
    ApiResponse<List<InstagramMediaDto>> getMedias(@NotNull @RequestParam(name = "instagram_id") Long instagramId);

}
