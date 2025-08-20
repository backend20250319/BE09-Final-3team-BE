package site.petful.userservice.controller;

import site.petful.userservice.dto.AuthResponse;
import site.petful.userservice.dto.LoginRequest;
import site.petful.userservice.dto.SignupRequest;
import site.petful.userservice.dto.TokenValidationRequest;
import site.petful.userservice.dto.TokenValidationResponse;
import site.petful.userservice.service.EmailService;
import site.petful.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        try {
            AuthResponse response = userService.signup(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody TokenValidationRequest request) {
        try {
            TokenValidationResponse response = userService.validateToken(request.getToken());
            if (response.isValid()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(TokenValidationResponse.builder()
                            .valid(false)
                            .message("토큰 검증에 실패했습니다: " + e.getMessage())
                            .build());
        }
    }


    @GetMapping("/profile")
    public ResponseEntity<AuthResponse> getProfile() {
        // 이 엔드포인트는 JWT 토큰이 필요합니다
        return ResponseEntity.ok(AuthResponse.builder()
                .message("프로필 조회 성공")
                .build());
    }

    @GetMapping("/my-info")
    public ResponseEntity<AuthResponse> getMyInfo() {
        // 이 엔드포인트는 JWT 토큰이 필요합니다
        return ResponseEntity.ok(AuthResponse.builder()
                .message("내 정보 조회 성공")
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        // 이 엔드포인트는 JWT 토큰이 필요합니다
        // 실제로는 클라이언트에서 토큰을 삭제하는 방식으로 처리
        return ResponseEntity.ok(AuthResponse.builder()
                .message("로그아웃 성공")
                .build());
    }
}
