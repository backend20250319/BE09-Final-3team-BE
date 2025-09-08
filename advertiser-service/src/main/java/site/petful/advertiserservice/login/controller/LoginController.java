package site.petful.advertiserservice.login.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.login.dto.LoginRequest;
import site.petful.advertiserservice.login.dto.LoginResponse;
import site.petful.advertiserservice.login.dto.PasswordResetRequest;
import site.petful.advertiserservice.login.dto.PasswordResetResponse;
import site.petful.advertiserservice.login.dto.PasswordResetVerifyRequest;
import site.petful.advertiserservice.login.dto.PasswordResetVerifyResponse;
import site.petful.advertiserservice.login.dto.PasswordResetChangeRequest;
import site.petful.advertiserservice.login.service.LoginService;
import site.petful.advertiserservice.login.service.PasswordResetEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/advertiser")
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    private final LoginService loginService;
    private final PasswordResetEmailService passwordResetEmailService;

    public LoginController(LoginService loginService, PasswordResetEmailService passwordResetEmailService) {
        this.loginService = loginService;
        this.passwordResetEmailService = passwordResetEmailService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("로그인 요청: {}", request.getUserId());
            LoginResponse response = loginService.login(request);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            log.error("로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, e.getMessage()));
        }
    }

    // 비밀번호 찾기 - 인증 코드 요청
    @PostMapping("/password/reset/request")
    public ResponseEntity<ApiResponse<?>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        try {
            log.info("비밀번호 재설정 인증 코드 요청: {}", request.getEmail());
            PasswordResetResponse response = loginService.requestPasswordReset(request);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            log.error("비밀번호 재설정 인증 코드 요청 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, e.getMessage()));
        }
    }

    // 인증번호 확인 (비밀번호 변경 없이)
    @PostMapping("/password/reset/verify")
    public ResponseEntity<ApiResponse<?>> verifyPasswordResetCode(@Valid @RequestBody PasswordResetVerifyRequest request) {
        try {
            log.info("인증번호 확인 요청 시작: {}", request.getEmail());
            log.info("인증 코드: {}", request.getVerificationCode());
            
            // PasswordResetEmailService 존재 여부 확인
            if (passwordResetEmailService == null) {
                log.error("PasswordResetEmailService가 null입니다!");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, "서비스 초기화 오류"));
            }
            
            PasswordResetVerifyResponse response = passwordResetEmailService.verifyCodeOnly(request.getEmail(), request.getVerificationCode());
            log.info("인증 코드 확인 완료: {}", response);
            
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (Exception e) {
            log.error("인증번호 확인 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, "인증번호 확인 실패: " + e.getMessage()));
        }
    }

    // 비밀번호 변경 (인증번호 확인 후)
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<?>> changePassword(@Valid @RequestBody PasswordResetChangeRequest request) {
        try {
            log.info("비밀번호 변경 요청: {}", request.getEmail());
            
            // 비밀번호 일치 여부 사전 검증
            if (!request.isPasswordMatching()) {
                log.warn("비밀번호 불일치: {}", request.getEmail());
                return ResponseEntity.badRequest()
                        .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."));
            }
            
            PasswordResetResponse response = loginService.changePasswordAfterVerification(request);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            log.error("비밀번호 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, e.getMessage()));
        }
    }
}

