package site.petful.snsservice.instagram.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.instagram.client.dto.InstagramProfileResponseDto;
import site.petful.snsservice.instagram.service.InstagramProfileService;

@RestController
@RequestMapping("/instagram/profiles")
@RequiredArgsConstructor
public class InstagramProfileController {

    private final InstagramProfileService instagramProfileService;

    //TODO [유저] 여기 userId 수정 쭉 들어가면서 userNo로 저장
    @PostMapping
    public ResponseEntity<ApiResponse<List<InstagramProfileResponseDto>>> syncProfiles(
        @RequestParam Long userId) {
        return ResponseEntity.ok(
            ApiResponseGenerator.success(instagramProfileService.syncInstagramProfiles(userId)));
    }
}
