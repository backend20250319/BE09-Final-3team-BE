package site.petful.communityservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import site.petful.communityservice.dto.UserBriefDto;
import site.petful.communityservice.security.FeignAuthConfig;

import java.util.List;

@FeignClient(name = "user-service", path = "/auth/profile",
configuration = FeignAuthConfig.class)
public interface UserClient {

    // 단건 조회
    @GetMapping("/simple")
    UserBriefDto getUserBrief(@RequestParam("userNo") Long userNo);

    // 다건 조회 (POST 방식 권장)
    @PostMapping("/simple/batch")
    List<UserBriefDto> getUsersBrief(@RequestBody List<Long> userNos);
}
