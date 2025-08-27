package site.petful.petservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8000}")
public interface UserClient {

    @GetMapping("/api/v1/user-service/auth/profile/{userNo}")
    UserProfileResponse getUserProfile(@PathVariable Long userNo, @RequestHeader("Authorization") String token);
}
