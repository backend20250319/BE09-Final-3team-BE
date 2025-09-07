package site.petful.advertiserservice.signup.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.common.AdvertiserHeaderUtil;
import site.petful.advertiserservice.signup.dto.*;
import site.petful.advertiserservice.signup.service.SignupService;

@RestController
@RequestMapping("/advertiser/signup")
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    // 1. 이메일 인증번호 발송
    @PostMapping("/email/send")
    public ResponseEntity<ApiResponse<?>> sendVerificationCode(@Valid @RequestBody EmailVerificationRequest request) {
        try {
            signupService.sendVerificationCode(request.getEmail());
            return ResponseEntity.ok(ApiResponseGenerator.success(
                EmailVerificationResponse.builder()
                    .message("인증번호가 발송되었습니다.")
                    .success(true)
                    .build()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, e.getMessage()));
        }
    }

    // 2. 이메일 인증번호 확인
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<?>> verifyEmailCode(@Valid @RequestBody EmailVerificationConfirmRequest request) {
        try {
            boolean isVerified = signupService.verifyEmailCode(request);
            if (isVerified) {
                return ResponseEntity.ok(ApiResponseGenerator.success(
                    EmailVerificationResponse.builder()
                        .message("이메일 인증이 완료되었습니다.")
                        .success(true)
                        .build()
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponseGenerator.fail(ErrorCode.INVALID_VERIFICATION_CODE, "인증번호가 올바르지 않습니다."));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, e.getMessage()));
        }
    }

    // 3. 광고주 회원가입
    @PostMapping
    public ResponseEntity<ApiResponse<?>> signup(@Valid @RequestBody SignupRequest request) {
        try {
            SignupResponse response = signupService.signup(request);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, e.getMessage()));
        }
    }

    // 4. 광고주 회원탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<?>> withdraw(@Valid @RequestBody WithdrawRequest request) {
        try {
            // 헤더에서 광고주 번호 추출
            Long advertiserNo = AdvertiserHeaderUtil.getCurrentAdvertiserNo();
            if (advertiserNo == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseGenerator.fail(ErrorCode.ADVERTISER_NOT_FOUND, "인증 정보가 없습니다. X-User-No 헤더를 확인해주세요."));
            }
            
            WithdrawResponse response = signupService.withdraw(advertiserNo, request);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.ADVERTISER_NOT_FOUND, e.getMessage()));
        }
    }
}



