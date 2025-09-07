package site.petful.petservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import site.petful.petservice.dto.InstagramProfileInfo;

import java.util.List;

@FeignClient(name = "sns-service", url = "${sns-service.url:http://localhost:8000}")
public interface InstagramProfileClient {
    
    // 사용자의 Instagram 프로필 목록 조회
    @GetMapping("/instagram/profiles/{userNo}")
    List<InstagramProfileInfo> getProfilesByUser(@PathVariable Long userNo);
    
    // 특정 Instagram 프로필 상세 조회
    @GetMapping("/instagram/profiles/{instagramId}")
    InstagramProfileInfo getProfile(@PathVariable Long instagramId);
}
