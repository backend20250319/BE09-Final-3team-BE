package site.petful.advertiserservice.admin.controlloer;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.advertiserservice.dto.advertiser.AdvertiserResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/advertiser")
@PreAuthorize("hasRole('ADMIN')")
public class AdvertiserAdminController {
    private final AdvertiserAdminController advertiserAdminController;

    @GetMapping("/all")
    public Page<AdvertiserResponse> getAllAdvertisers(
            @RequestHeader("X-User-No") Long userNo,
            @RequestHeader("X-User-Type")String userType,
            @PageableDefault(
                    size = 5,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ){

    }
}
