package site.petful.notificationservice.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import site.petful.notificationservice.common.ApiResponse;
import site.petful.notificationservice.dto.SimpleProfileResponse;
import site.petful.notificationservice.security.FeignAuthConfig;

import java.util.List;

@FeignClient(name = "user-service", path = "/auth/profile",
configuration = FeignAuthConfig.class)
public interface UserClient {

    // 단건 조회
    @GetMapping("/simple")
    ApiResponse<SimpleProfileResponse> getUserBrief(@RequestParam("userNo") Long userNo);

    // 다건 조회 (POST 방식 권장)
    @PostMapping("/simple/batch")
    ApiResponse<List<SimpleProfileResponse>> getUsersBrief(@RequestBody List<Long> userNos);
    
    // 헬스체크 (user-service가 정상 작동하는지 확인)
    @GetMapping("/health")
    String healthCheck();
}
