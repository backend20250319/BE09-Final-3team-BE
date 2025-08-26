package site.petful.userservice.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name="ADVERTISER-SERVICE"
)
public interface AdvertiserClient {
    @PostMapping("/admin/advertisers/{id}/blacklist")
    void blacklistAdvertiser(
            @PathVariable("id") Long advertiesrId
    );
}
