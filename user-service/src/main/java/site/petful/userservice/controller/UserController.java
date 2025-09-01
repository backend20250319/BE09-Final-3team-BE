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
import site.petful.userservice.dto.WithdrawRequest;
import site.petful.userservice.dto.WithdrawResponse;
import site.petful.userservice.dto.LogoutRequest;
import site.petful.userservice.dto.TokenInfoResponse;
import site.petful.userservice.service.AuthService;
import site.petful.userservice.service.UserService;
import site.petful.userservice.security.JwtUtil;
import site.petful.userservice.common.UserHeaderUtil;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;              // 회원가입/유저 관련
    private final AuthService authService;              // 토큰 발급/리프레시
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

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
        long accessExpiresAt = now + Duration.ofMinutes(authService.accessTtlMinutes()).toMillis();
        long refreshExpiresAt = now + Duration.ofDays(authService.refreshTtlDays()).toMillis();
        
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .accessExpiresAt(accessExpiresAt)
                .refreshExpiresAt(refreshExpiresAt)
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
        try {
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
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<AuthResponse>(ErrorCode.INVALID_REQUEST, "토큰 재발급 실패: " + e.getMessage(), null));
        }
    }

    /**
     * 토큰 유효성 검증
     * POST /auth/validate-token
     */
    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(ApiResponseGenerator.success(false));
            }
            
            String token = authHeader.substring(7);
            boolean isValid = jwtUtil.validateAccessToken(token);
            
            return ResponseEntity.ok(ApiResponseGenerator.success(isValid));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponseGenerator.success(false));
        }
    }

    /**
     * 토큰 상태 확인 및 자동 갱신
     * POST /auth/check-token
     */
    @PostMapping("/check-token")
    public ResponseEntity<ApiResponse<TokenInfoResponse>> checkToken(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String refreshToken) {
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(ErrorCode.INVALID_REQUEST, "액세스 토큰이 없습니다.", null));
            }
            
            String accessToken = authHeader.substring(7);
            
            // 액세스 토큰이 유효한지 확인
            if (jwtUtil.validateAccessToken(accessToken)) {
                // 액세스 토큰이 유효하면 현재 토큰 정보 반환
                return ResponseEntity.ok(ApiResponseGenerator.success(TokenInfoResponse.builder()
                        .message("토큰이 유효합니다.")
                        .tokenType("valid")
                        .build()));
            } else {
                // 액세스 토큰이 만료되었고, 리프레시 토큰이 제공된 경우
                if (refreshToken != null && !refreshToken.trim().isEmpty()) {
                    try {
                        // 새로운 토큰 발급
                        String newAccess = authService.refreshAccess(refreshToken);
                        String newRefresh = authService.rotateRefresh(refreshToken);
                        
                        long now = System.currentTimeMillis();
                        long accessExpiresAt = now + Duration.ofMinutes(authService.accessTtlMinutes()).toMillis();
                        long refreshExpiresAt = now + Duration.ofDays(authService.refreshTtlDays()).toMillis();
                        
                        TokenInfoResponse response = TokenInfoResponse.builder()
                                .accessToken(newAccess)
                                .refreshToken(newRefresh)
                                .accessExpiresAt(LocalDateTime.now().plusMinutes(authService.accessTtlMinutes()))
                                .refreshExpiresAt(LocalDateTime.now().plusDays(authService.refreshTtlDays()))
                                .accessExpiresIn(authService.accessTtlMinutes() * 60)
                                .refreshExpiresIn(authService.refreshTtlDays() * 24 * 60 * 60)
                                .tokenType("refreshed")
                                .message("토큰이 갱신되었습니다.")
                                .build();
                        
                        return ResponseEntity.ok(ApiResponseGenerator.success(response));
                    } catch (Exception e) {
                        return ResponseEntity.badRequest().body(new ApiResponse<>(ErrorCode.INVALID_REQUEST, "리프레시 토큰이 유효하지 않습니다.", null));
                    }
                } else {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(ErrorCode.INVALID_REQUEST, "액세스 토큰이 만료되었습니다. 리프레시 토큰을 제공해주세요.", null));
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(ErrorCode.INVALID_REQUEST, "토큰 검증 중 오류가 발생했습니다.", null));
        }
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보 조회
     * GET /auth/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        // 헤더에서 사용자 번호를 먼저 시도
        Long userNo = UserHeaderUtil.getCurrentUserNo();
        
        // 헤더가 없으면 기존 JWT 방식으로 fallback
        if (userNo == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                User user = userService.findByEmail(email);
                userNo = user.getUserNo();
            } else {
                throw new IllegalStateException("인증 정보가 없습니다.");
            }
        }
        
        ProfileResponse profile = userService.getProfile(userNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(profile));
    }
    
    /**
     * 현재 로그인한 사용자의 프로필 정보 수정
     * Patch /auth/profile
     */
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        // 헤더에서 사용자 번호를 먼저 시도
        Long userNo = UserHeaderUtil.getCurrentUserNo();
        
        // 헤더가 없으면 기존 JWT 방식으로 fallback
        if (userNo == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                User user = userService.findByEmail(email);
                userNo = user.getUserNo();
            } else {
                throw new IllegalStateException("인증 정보가 없습니다.");
            }
        }
        
        ProfileResponse updatedProfile = userService.updateProfile(userNo, request);
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


    @PostMapping("/profile/simple/batch")
    public ResponseEntity<ApiResponse<List<SimpleProfileResponse>>> getSimpleProfilesBatch(@RequestBody List<Long> userNos) {
        List<SimpleProfileResponse> profiles = userService.getSimpleProfilesBatch(userNos);
        return ResponseEntity.ok(ApiResponseGenerator.success(profiles));
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
        
        Long userNo = UserHeaderUtil.getCurrentUserNoOrThrow();
        FileUploadResponse response = userService.uploadProfileImage(file, userNo);
        
        if (response.isSuccess()) {
            // 업데이트된 프로필 정보가 포함된 응답 반환
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>(ErrorCode.INVALID_REQUEST, response.getMessage(), response));
        }
    }
    
    /**
     * 회원탈퇴
     * DELETE /auth/withdraw
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<WithdrawResponse>> withdraw(@Valid @RequestBody WithdrawRequest request) {
        Long userNo = UserHeaderUtil.getCurrentUserNoOrThrow();
        WithdrawResponse response = userService.withdraw(userNo, request);
        
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }

    /**
     * 현재 인증된 사용자 정보 확인 (테스트용)
     * GET /auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        Long userNo = UserHeaderUtil.getCurrentUserNoOrThrow();
        String userType = UserHeaderUtil.getCurrentUserType();
        
        User user = userService.findByUserNo(userNo);
        
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .userNo(user.getUserNo())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .userType(user.getUserType().name())
                .headerUserType(userType)
                .build();
        
        return ResponseEntity.ok(ApiResponseGenerator.success(userInfo));
    }
    
    /**
     * 로그아웃
     * POST /auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@Valid @RequestBody LogoutRequest request) {
        try {
            // 리프레시 토큰 검증 및 로그아웃 처리
            authService.logout(request.getRefreshToken());
            
            return ResponseEntity.ok(ApiResponseGenerator.success("로그아웃이 성공적으로 처리되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(ErrorCode.INVALID_REQUEST, e.getMessage(), null));
        }
    }
}
