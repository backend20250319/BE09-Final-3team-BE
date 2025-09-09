package site.petful.advertiserservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.dto.InstagramProfileDto;
import site.petful.advertiserservice.service.InstagramService;

import java.util.List;

@RestController
@RequestMapping("/instagram")
public class InstagramController {

    private final InstagramService instagramService;

    public InstagramController(InstagramService instagramService) {
        this.instagramService = instagramService;
    }

    // 1. 사용자 instagram 프로필 조회
    @GetMapping("/{userNo}")
    public ResponseEntity<ApiResponse<List<InstagramProfileDto>>> getProfileExternal(@PathVariable Long userNo) {
        List<InstagramProfileDto> profilesResponseDto = instagramService.getProfile(userNo);

        return ResponseEntity.ok(ApiResponseGenerator.success(profilesResponseDto));
    }

    // 2. 사용자 instagram 프로필 조회 by snsId
    @GetMapping("/influencer/{snsId}")
    public ResponseEntity<ApiResponse<InstagramProfileDto>> getProfileBySnSId(@PathVariable Long snsId) {
        InstagramProfileDto profilesResponseDto = instagramService.getProfileBySnSId(snsId);

        return ResponseEntity.ok(ApiResponseGenerator.success(profilesResponseDto));
    }
}
