package site.petful.userservice.controller;

import io.jsonwebtoken.Claims;
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
import site.petful.userservice.domain.User;
import site.petful.userservice.dto.*;
import site.petful.userservice.service.AuthService;
import site.petful.userservice.service.UserService;
import site.petful.userservice.common.ErrorCode;
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
        site.petful.userservice.domain.User user = userService.findByEmail(username);

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
     * 현재 로그인한 사용자 정보 조회
     * GET /auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userService.findByEmail(email);
        
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .userNo(user.getUserNo())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .role(user.getUserType().name())
                .build();
        
        return ResponseEntity.ok(ApiResponseGenerator.success(userInfo));
    }

    /**
     * 로그아웃(무상태)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponseGenerator.success("로그아웃 성공"));
    }
}
