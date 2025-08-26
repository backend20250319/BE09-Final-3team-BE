package site.petful.snsservice.instagram.profile.controller;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.instagram.auth.service.InstagramTokenService;
import site.petful.snsservice.instagram.insight.service.InstagramFollowerHistoryService;
import site.petful.snsservice.instagram.profile.dto.InstagramProfileDto;
import site.petful.snsservice.instagram.profile.service.InstagramProfileService;
import site.petful.snsservice.util.DateTimeUtils;

@RestController
@RequestMapping("/instagram/profiles")
@RequiredArgsConstructor
public class InstagramProfileController {

    private final InstagramProfileService instagramProfileService;
    private final InstagramTokenService instagramTokenService;
    private final InstagramFollowerHistoryService instagramFollowerHistoryService;

    @GetMapping()
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


    //TODO [유저] 여기 userId 수정 쭉 들어가면서 userNo로 저장
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<List<InstagramProfileDto>>> syncProfiles(
        @RequestParam(name = "user_no") Long userNo) {
        String accessToken = instagramTokenService.getAccessToken(userNo);
        List<InstagramProfileDto> profiles = instagramProfileService.syncAllInstagramProfiles(
            userNo,
            accessToken);

        for (InstagramProfileDto profile : profiles) {
            instagramFollowerHistoryService.saveFollowerHistory(profile.id(),
                DateTimeUtils.getStartOfCurrentMonth().toLocalDate(), profile.followers_count());
        }

        return ResponseEntity.ok(
            ApiResponseGenerator.success(
                profiles
            ));
    }


    @PostMapping("/auto-delete")
    public ResponseEntity<ApiResponse<Void>> autoDeleteComments(
        @RequestParam(name = "instagram_id") Long instagramId, @RequestParam Boolean isAutoDelete) {

        instagramProfileService.setAutoDelete(instagramId, isAutoDelete);

        return ResponseEntity.ok(ApiResponseGenerator.success(null));
    }

}
