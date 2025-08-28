package site.petful.userservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import site.petful.userservice.common.ApiResponse;
import site.petful.userservice.common.ApiResponseGenerator;
import site.petful.userservice.common.ErrorCode;
import site.petful.userservice.entity.User;
import site.petful.userservice.dto.*;
import site.petful.userservice.dto.PasswordChangeRequest;
import site.petful.userservice.dto.PasswordResetRequest;
import site.petful.userservice.dto.PasswordResetResponse;
import site.petful.userservice.dto.VerificationConfirmRequest;
import site.petful.userservice.dto.VerificationConfirmResponse;
import site.petful.userservice.dto.FileUploadResponse;
import site.petful.userservice.dto.ProfileResponse;
import site.petful.userservice.dto.ProfileUpdateRequest;
import site.petful.userservice.dto.SimpleProfileResponse;
import site.petful.userservice.service.AuthService;
import site.petful.userservice.service.UserService;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;              // 회원가입/유저 관련
    private final AuthService authService;              // 토큰 발급/리프레시
    private final AuthenticationManager authenticationManager;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse res = userService.signup(request);
        return ResponseEntity.status(201).body(ApiResponseGenerator.success(res));
    }

    /**
     * /auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        // 인증
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        final String username = auth.getName(); // 보통 email

        // 유저 조회 후 Access/Refresh 발급
        site.petful.userservice.entity.User user = userService.findByEmail(username);

        log.debug("로그인 사용자 정보 email={}, name={}, nickname={}, userNo={}",
                user.getEmail(), user.getName(), user.getNickname(), user.getUserNo());

        String access = authService.issueAccess(user);     // userNo/userType 포함
        String refresh = authService.issueRefresh(username);

        long now = System.currentTimeMillis();
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .accessExpiresAt(now + Duration.ofMinutes(authService.accessTtlMinutes()).toMillis())
                .refreshExpiresAt(now + Duration.ofDays(authService.refreshTtlDays()).toMillis())
                .email(username)
                .name(user.getNickname() != null ? user.getNickname() : user.getName())
                .message("로그인 성공")
                .build();

        return ResponseEntity.ok(ApiResponseGenerator.success(authResponse));
    }

    /**
     * /auth/refresh : 클라이언트가 보낸 RT로 새 AT(+선택적 롤링 RT) 발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest req) {
        long now = System.currentTimeMillis();

        String newAccess = authService.refreshAccess(req.getRefreshToken());
        // 필요 시에만 롤링. 정책상 롤링을 사용하지 않으려면 아래 한 줄을 제거/주석 처리하면 된다.
        String newRefresh = authService.rotateRefresh(req.getRefreshToken());

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .accessExpiresAt(now + Duration.ofMinutes(authService.accessTtlMinutes()).toMillis())
                .refreshExpiresAt(now + Duration.ofDays(authService.refreshTtlDays()).toMillis())
                .message("토큰 재발급 성공")
                .build();

        return ResponseEntity.ok(ApiResponseGenerator.success(authResponse));
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보 조회
     * GET /auth/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userService.findByEmail(email);
        ProfileResponse profile = userService.getProfile(user.getUserNo());
        
        return ResponseEntity.ok(ApiResponseGenerator.success(profile));
    }
    
    /**
     * 현재 로그인한 사용자의 프로필 정보 수정
     * Patch /auth/profile
     */
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userService.findByEmail(email);
        ProfileResponse updatedProfile = userService.updateProfile(user.getUserNo(), request);
        
        return ResponseEntity.ok(ApiResponseGenerator.success(updatedProfile));
    }
    
    /**
     * 사용자의 간단한 프로필 정보 조회 (닉네임, 프로필 이미지)
     * GET /auth/profile/simple?userNo={userNo}
     */
    @GetMapping("/profile/simple")
    public ResponseEntity<ApiResponse<SimpleProfileResponse>> getSimpleProfile(@RequestParam Long userNo) {
        SimpleProfileResponse simpleProfile = userService.getSimpleProfile(userNo);
        
        return ResponseEntity.ok(ApiResponseGenerator.success(simpleProfile));
    }
    
    /**
     * 비밀번호 재설정 요청
     * POST /auth/password/reset
     */
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<PasswordResetResponse>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        PasswordResetResponse response = userService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }
    
    /**
     * 비밀번호 재설정 인증번호 확인
     * POST /auth/password/verify
     */
    @PostMapping("/password/verify")
    public ResponseEntity<ApiResponse<VerificationConfirmResponse>> verifyPasswordResetCode(@Valid @RequestBody VerificationConfirmRequest request) {
        VerificationConfirmResponse response = userService.verifyPasswordResetCode(request);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }
    
    /**
     * 비밀번호 변경
     * POST /auth/password/change
     */
    @PostMapping("/password/change")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponseGenerator.success("비밀번호가 성공적으로 변경되었습니다."));
    }

    /**
     * 프로필 이미지 업로드
     * POST /auth/profile/image
     */
    @PostMapping("/profile/image")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadProfileImage(
            @RequestParam("file") MultipartFile file) {
        
        // Spring Security의 Authentication을 사용하여 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        // 이메일로 사용자 조회
        User user = userService.findByEmail(email);
        Long userNo = user.getUserNo();
        
        FileUploadResponse response = userService.uploadProfileImage(file, userNo);
        
        if (response.isSuccess()) {
            // 업데이트된 프로필 정보가 포함된 응답 반환
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } else {
            return ResponseEntity.badRequest().body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, response.getMessage(), response));
        }
    }
    
    /**
     * 로그아웃(무상태)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponseGenerator.success("로그아웃 성공"));
    }
}
