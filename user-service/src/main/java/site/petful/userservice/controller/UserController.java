package site.petful.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.petful.userservice.common.ApiResponse;
import site.petful.userservice.common.ApiResponseGenerator;
import site.petful.userservice.dto.*;
import site.petful.userservice.service.AuthService;
import site.petful.userservice.service.UserService;

import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse res = userService.signup(request);
        return ResponseEntity.status(201).body(ApiResponseGenerator.success(res));
    }

    /** /auth/login */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // User 객체를 가져와서 토큰 생성
        site.petful.userservice.domain.User user = userService.findByEmail(request.getEmail());
        
        // 디버깅용 로그 추가
        System.out.println("로그인 사용자 정보:");
        System.out.println("Email: " + user.getEmail());
        System.out.println("Name: " + user.getName());
        System.out.println("Nickname: " + user.getNickname());
        System.out.println("UserNo: " + user.getUserNo());
        
        long now = System.currentTimeMillis();
        String access  = authService.issueAccess(user); // User 객체로 토큰 생성
        String refresh = authService.issueRefresh(auth.getName());

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .accessExpiresAt(now + Duration.ofMinutes(authService.accessTtlMinutes()).toMillis())
                .refreshExpiresAt(now + Duration.ofDays(authService.refreshTtlDays()).toMillis())
                .email(request.getEmail())
                .name(user.getNickname() != null ? user.getNickname() : user.getName()) // 닉네임이 없으면 이름 사용
                .message("로그인 성공")
                .build();

        return ResponseEntity.ok(ApiResponseGenerator.success(authResponse));
    }

    /** /auth/refresh : 클라이언트가 보낸 RT로 새 AT(+롤링 RT) 발급 */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest req) {
        long now = System.currentTimeMillis();

        String newAccess  = authService.refreshAccess(req.getRefreshToken());
        // 필요 시 롤링(새 RT 발급). 재사용 원하면 rotateRefresh 호출을 제거하면 됨.
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

    /** 보호된 예시 엔드포인트 */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<String>> getProfile() {
        return ResponseEntity.ok(ApiResponseGenerator.success("프로필 조회 성공"));
    }

    @GetMapping("/my-info")
    public ResponseEntity<ApiResponse<String>> getMyInfo() {
        return ResponseEntity.ok(ApiResponseGenerator.success("내 정보 조회 성공"));
    }

    /** 로그아웃(무상태) */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponseGenerator.success("로그아웃 성공"));
    }

    /** 토큰 내용 확인용 테스트 엔드포인트 */
    @PostMapping("/decode-token")
    public ResponseEntity<ApiResponse<Object>> decodeToken(@RequestBody String token) {
        io.jsonwebtoken.Claims claims = authService.getJwtUtil().parseAccessClaims(token);
        return ResponseEntity.ok(ApiResponseGenerator.success(claims));
    }
}
