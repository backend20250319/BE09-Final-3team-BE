package site.petful.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.petful.userservice.dto.*;
import site.petful.userservice.service.AuthService;
import site.petful.userservice.service.UserService;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 게이트웨이에서 전역 CORS를 관리한다면 삭제해도 됨
public class UserController {

    private final UserService userService;              // 회원가입/유저 관련
    private final AuthService authService;              // 토큰 발급/리프레시
    private final AuthenticationManager authenticationManager;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
        try {
            SignupResponse res = userService.signup(request);  // ← 반환 타입 SignupResponse
            return ResponseEntity.status(201).body(res);       // 201 Created
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(409)
                    .body(SignupResponse.builder().message("이미 존재하는 이메일입니다.").build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SignupResponse.builder().message(e.getMessage()).build());
        }
    }

    /** /auth/login */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        long now = System.currentTimeMillis();
        String access  = authService.issueAccess(auth.getName());
        String refresh = authService.issueRefresh(auth.getName());

        return ResponseEntity.ok(
                AuthResponse.builder()
                        .accessToken(access)
                        .refreshToken(refresh)
                        .accessExpiresAt(now + Duration.ofMinutes(authService.accessTtlMinutes()).toMillis())
                        .refreshExpiresAt(now + Duration.ofDays(authService.refreshTtlDays()).toMillis())
                        .email(request.getEmail())
                        .message("로그인 성공")
                        .build()
        );
    }

    /** /auth/refresh : 클라이언트가 보낸 RT로 새 AT(+롤링 RT) 발급 */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest req) {
        long now = System.currentTimeMillis();

        String newAccess  = authService.refreshAccess(req.getRefreshToken());
        // 필요 시 롤링(새 RT 발급). 재사용 원하면 rotateRefresh 호출을 제거하면 됨.
        String newRefresh = authService.rotateRefresh(req.getRefreshToken());

        return ResponseEntity.ok(
                AuthResponse.builder()
                        .accessToken(newAccess)
                        .refreshToken(newRefresh)
                        .accessExpiresAt(now + Duration.ofMinutes(authService.accessTtlMinutes()).toMillis())
                        .refreshExpiresAt(now + Duration.ofDays(authService.refreshTtlDays()).toMillis())
                        .message("토큰 재발급 성공")
                        .build()
        );
    }

    /** 액세스 토큰 검증 (기존 유지) */
    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody TokenValidationRequest request) {
        try {
            TokenValidationResponse response = userService.validateToken(request.getToken());
            if (response.isValid()) return ResponseEntity.ok(response);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(TokenValidationResponse.builder()
                            .valid(false)
                            .message("토큰 검증에 실패했습니다: " + e.getMessage())
                            .build());
        }
    }

    /** 보호된 예시 엔드포인트 */
    @GetMapping("/profile")
    public ResponseEntity<AuthResponse> getProfile() {
        return ResponseEntity.ok(AuthResponse.builder().message("프로필 조회 성공").build());
    }

    @GetMapping("/my-info")
    public ResponseEntity<AuthResponse> getMyInfo() {
        return ResponseEntity.ok(AuthResponse.builder().message("내 정보 조회 성공").build());
    }

    /** 로그아웃(무상태) */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        return ResponseEntity.ok(AuthResponse.builder().message("로그아웃 성공").build());
    }
}
