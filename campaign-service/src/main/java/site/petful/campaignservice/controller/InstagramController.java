package site.petful.campaignservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.common.ApiResponseGenerator;
import site.petful.campaignservice.dto.InstagramProfileDto;
import site.petful.campaignservice.service.InstagramService;

import java.util.List;

@RestController
@RequestMapping("/instagram")
public class InstagramController {

    private final InstagramService instagramService;

    public InstagramController(InstagramService instagramService) {
        this.instagramService = instagramService;
    }

    // 1. 사용자 instagram 프로필 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<InstagramProfileDto>>> getProfile() {
        List<InstagramProfileDto> profilesResponseDto = instagramService.getProfile();

        return ResponseEntity.ok(ApiResponseGenerator.success(profilesResponseDto));
    }
}
