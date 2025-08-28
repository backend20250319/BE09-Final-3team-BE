package site.petful.snsservice.instagram.profile.controller;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.instagram.auth.service.InstagramTokenService;
import site.petful.snsservice.instagram.profile.dto.InstagramProfileDto;
import site.petful.snsservice.instagram.profile.service.InstagramProfileService;

@Slf4j
@RestController
@RequestMapping("/instagram/profiles")
@RequiredArgsConstructor
public class InstagramProfileController {

    private final InstagramProfileService instagramProfileService;
    private final InstagramTokenService instagramTokenService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InstagramProfileDto>>> getProfiles(
        @NotNull @RequestParam(name = "user_no") Long userNO) {
        List<InstagramProfileDto> profilesResponseDto = instagramProfileService.getProfiles(
            userNO);

        return ResponseEntity.ok(ApiResponseGenerator.success(profilesResponseDto));
    }

    @GetMapping("/{instagramId}")
    public ResponseEntity<ApiResponse<InstagramProfileDto>> getProfile(
        @NotNull @PathVariable Long instagramId) {

        InstagramProfileDto profileResponseDto = instagramProfileService.getProfile(
            instagramId);

        return ResponseEntity.ok(ApiResponseGenerator.success(profileResponseDto));
    }


    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<List<InstagramProfileDto>>> syncProfiles(
        @RequestParam(name = "user_no") Long userNo) {
        String accessToken = instagramTokenService.getAccessToken(userNo);
        List<InstagramProfileDto> profiles = instagramProfileService.syncAllInstagramProfiles(
            userNo,
            accessToken);

        return ResponseEntity.ok(
            ApiResponseGenerator.success(
                profiles
            ));
    }


    @PreAuthorize("hasAuthority('User')")
    @PostMapping("/auto-delete")
    public ResponseEntity<ApiResponse<Void>> autoDeleteComments(
        @AuthenticationPrincipal String userNo,
        @RequestParam(name = "instagram_id") Long instagramId, @RequestParam Boolean isAutoDelete) {

        System.out.println("userNo = " + userNo);

        instagramProfileService.setAutoDelete(Long.parseLong(userNo), instagramId, isAutoDelete);

        return ResponseEntity.ok(ApiResponseGenerator.success(null));
    }

}
