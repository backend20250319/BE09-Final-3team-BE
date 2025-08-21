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

        // User 객체를 가져와서 토큰 생성
        site.petful.userservice.domain.User user = userService.findByEmail(request.getEmail());
        
        long now = System.currentTimeMillis();
        String access  = authService.issueAccess(user); // User 객체로 토큰 생성
        String refresh = authService.issueRefresh(auth.getName());

        return ResponseEntity.ok(
                AuthResponse.builder()
                        .accessToken(access)
                        .refreshToken(refresh)
                        .accessExpiresAt(now + Duration.ofMinutes(authService.accessTtlMinutes()).toMillis())
                        .refreshExpiresAt(now + Duration.ofDays(authService.refreshTtlDays()).toMillis())
                        .email(request.getEmail())
                        .name(user.getNickname()) // 닉네임 추가
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

    /** 토큰 내용 확인용 테스트 엔드포인트 */
    @PostMapping("/decode-token")
    public ResponseEntity<?> decodeToken(@RequestBody String token) {
        try {
            io.jsonwebtoken.Claims claims = authService.getJwtUtil().parseAccessClaims(token);
            return ResponseEntity.ok(claims);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("토큰 디코딩 실패: " + e.getMessage());
        }
    }
}
