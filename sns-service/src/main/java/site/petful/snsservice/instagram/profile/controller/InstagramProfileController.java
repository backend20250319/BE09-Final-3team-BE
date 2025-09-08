package site.petful.snsservice.instagram.profile.controller;

import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.instagram.auth.service.InstagramTokenService;
import site.petful.snsservice.instagram.profile.dto.AutoDeleteRequestDto;
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
        @AuthenticationPrincipal String userNo) {
        List<InstagramProfileDto> profilesResponseDto = instagramProfileService.getProfiles(
            Long.parseLong(userNo)
        );

        return ResponseEntity.ok(ApiResponseGenerator.success(profilesResponseDto));
    }

    @GetMapping("/advertiser/{userNo}")
    public ResponseEntity<ApiResponse<List<InstagramProfileDto>>> getProfileExternal(
        @NotNull @PathVariable Long userNo) {
        List<InstagramProfileDto> profilesResponseDto = instagramProfileService.getProfiles(userNo);

        return ResponseEntity.ok(ApiResponseGenerator.success(profilesResponseDto));
    }

    @GetMapping("/{instagramId}")
    public ResponseEntity<ApiResponse<InstagramProfileDto>> getProfile(
        @NotNull @PathVariable Long instagramId) {

        InstagramProfileDto profileResponseDto = instagramProfileService.getProfile(
            instagramId);

        return ResponseEntity.ok(ApiResponseGenerator.success(profileResponseDto));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
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


    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/auto-delete")
    public ResponseEntity<ApiResponse<Void>> autoDeleteComments(
        @AuthenticationPrincipal String userNo,
        @Valid @RequestBody AutoDeleteRequestDto request) {

        instagramProfileService.setAutoDelete(Long.parseLong(userNo), request.instagramId(),
            request.isAutoDelete());

        return ResponseEntity.ok(ApiResponseGenerator.success(null));
    }

}
