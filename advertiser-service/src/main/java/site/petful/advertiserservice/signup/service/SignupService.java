package site.petful.advertiserservice.signup.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.signup.dto.EmailVerificationConfirmRequest;
import site.petful.advertiserservice.signup.dto.SignupRequest;
import site.petful.advertiserservice.signup.dto.SignupResponse;
import site.petful.advertiserservice.signup.entity.AdvertiserSignup;
import site.petful.advertiserservice.signup.repository.AdvertiserSignupRepository;
import site.petful.advertiserservice.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final AdvertiserSignupRepository advertiserSignupRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final JwtTokenProvider jwtTokenProvider;

    // 이메일 인증번호 발송
    public void sendVerificationCode(String email) {
        emailVerificationService.sendVerificationCode(email);
    }

    // 이메일 인증번호 확인
    public boolean verifyEmailCode(EmailVerificationConfirmRequest request) {
        return emailVerificationService.verifyCode(request.getEmail(), request.getCode());
    }

    // 광고주 회원가입
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 이메일 중복 확인
        if (advertiserSignupRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException(ErrorCode.EMAIL_ALREADY_EXISTS.getDefaultMessage());
        }

        // 광고주 엔티티 생성
        AdvertiserSignup advertiser = new AdvertiserSignup();
        advertiser.setUserId(request.getUserId());
        advertiser.setPassword(passwordEncoder.encode(request.getPassword()));
        advertiser.setName(request.getName());
        advertiser.setPhone(request.getPhone());
        advertiser.setDescription(request.getDescription());
        advertiser.setImageUrl(request.getImageUrl());
        advertiser.setDocUrl(request.getDocUrl());
        advertiser.setIsActive(true);
        advertiser.setIsApproved(false);

        AdvertiserSignup savedAdvertiser = advertiserSignupRepository.save(advertiser);

        // JWT 토큰 생성 (Access Token + Refresh Token)
        String accessToken = jwtTokenProvider.generateAccessToken(savedAdvertiser.getAdvertiserNo(), "ADVERTISER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedAdvertiser.getAdvertiserNo(), "ADVERTISER");

        return SignupResponse.builder()
                .userNo(savedAdvertiser.getAdvertiserNo())
                .userType("ADVERTISER")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .message("회원가입이 완료되었습니다.")
                .build();
    }
}
