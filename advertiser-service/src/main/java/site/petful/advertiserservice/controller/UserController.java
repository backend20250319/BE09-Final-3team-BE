package site.petful.advertiserservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.dto.ProfileResponse;
import site.petful.advertiserservice.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. 사용자 프로필 조회
    @GetMapping("/profile/{userNo}")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@PathVariable String userNo) {
        ProfileResponse profile = userService.getProfile(userNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(profile));
    }

}
