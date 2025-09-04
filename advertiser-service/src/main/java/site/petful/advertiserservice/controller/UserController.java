package site.petful.advertiserservice.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.dto.ProfileResponse;
import site.petful.advertiserservice.dto.ReportRequest;
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

    // 2. 사용자 신고하기
    @PostMapping("/reports")
    public ResponseEntity<ApiResponse<?>> reportUser(@AuthenticationPrincipal String advertiserNo,
                                                          @Valid @RequestBody ReportRequest request){

        try {
            userService.reportUser(Long.valueOf(advertiserNo), request);
            return ResponseEntity.ok(ApiResponseGenerator.success("신고가 성공적으로 접수되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST));
        }
    }

}
