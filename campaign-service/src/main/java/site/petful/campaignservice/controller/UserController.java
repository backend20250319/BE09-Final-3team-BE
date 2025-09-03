package site.petful.campaignservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.common.ApiResponseGenerator;
import site.petful.campaignservice.dto.ProfileResponse;
import site.petful.campaignservice.security.SecurityUtil;
import site.petful.campaignservice.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;

    public UserController(UserService userService, SecurityUtil securityUtil) {
        this.userService = userService;
        this.securityUtil = securityUtil;
    }

    // 1. 사용자 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        Long userNo = securityUtil.getCurrentUserNo();
        ProfileResponse profile = userService.getProfile(userNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(profile));
    }

}
