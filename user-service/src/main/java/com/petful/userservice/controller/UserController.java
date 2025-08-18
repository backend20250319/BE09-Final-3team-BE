package com.petful.userservice.controller;

import com.petful.userservice.dto.AuthResponse;
import com.petful.userservice.dto.EmailVerificationConfirmRequest;
import com.petful.userservice.dto.EmailVerificationRequest;
import com.petful.userservice.dto.LoginRequest;
import com.petful.userservice.dto.SignupRequest;
import com.petful.userservice.service.EmailService;
import com.petful.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
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

    @PostMapping("/send-verification")
    public ResponseEntity<AuthResponse> sendVerificationEmail(@RequestBody EmailVerificationRequest request) {
        try {
            emailService.sendVerificationEmail(request);
            return ResponseEntity.ok(AuthResponse.builder()
                    .message("인증 이메일이 발송되었습니다.")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                            .message("인증 이메일 발송에 실패했습니다: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestBody EmailVerificationConfirmRequest request) {
        try {
            boolean isVerified = emailService.verifyEmailCode(request);
            if (isVerified) {
                return ResponseEntity.ok(AuthResponse.builder()
                        .message("이메일 인증이 완료되었습니다.")
                        .build());
            } else {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.builder()
                                .message("인증 코드가 올바르지 않습니다.")
                                .build());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                            .message("이메일 인증에 실패했습니다: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("User Service is running!");
    }
}
