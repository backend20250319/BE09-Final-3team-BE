package site.petful.communityservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import site.petful.communityservice.dto.UserBriefDto;

import java.util.List;

@FeignClient(name = "USER-SERVICE",               // ★ Eureka 서비스 ID
        path = "/users")
public interface UserClient {
    @GetMapping("{id}/breif")
    UserBriefDto getUserBrief(@PathVariable Long userNo);

    @GetMapping("breif")
    List<UserBriefDto> getUsersBrief(@RequestParam List<Long> userNos);
}
