package site.petful.advertiserservice.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.petful.advertiserservice.admin.service.AdvertiserAdminService;

@RestController
@RequestMapping("/admin/advertisers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdvertiserAdminConrtoller {
    private final AdvertiserAdminService advertiserAdminService;

    @PostMapping("/{id}/restrict")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restrict(
            @PathVariable("id") Long id
    ){
        advertiserAdminService.restrictAdvertiser(id);
    }
}
