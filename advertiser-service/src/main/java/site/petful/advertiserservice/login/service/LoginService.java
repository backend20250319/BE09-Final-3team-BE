package site.petful.advertiserservice.login.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import site.petful.advertiserservice.login.dto.LoginRequest;
import site.petful.advertiserservice.login.dto.LoginResponse;
import site.petful.advertiserservice.security.JwtTokenProvider;
import site.petful.advertiserservice.signup.entity.AdvertiserSignup;
import site.petful.advertiserservice.signup.repository.AdvertiserSignupRepository;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AdvertiserSignupRepository advertiserSignupRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        // 사용자 조회
        AdvertiserSignup advertiser = advertiserSignupRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다."));

        // 계정 활성화 상태 확인
        if (!advertiser.getIsActive()) {
            throw new RuntimeException("비활성화된 계정입니다.");
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), advertiser.getPassword())) {
            throw new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // JWT 토큰 생성 (Access Token + Refresh Token)
        String accessToken = jwtTokenProvider.generateAccessToken(advertiser.getAdvertiserNo(), "ADVERTISER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(advertiser.getAdvertiserNo(), "ADVERTISER");

        return LoginResponse.builder()
                .advertiserNo(advertiser.getAdvertiserNo())
                .userType("ADVERTISER")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .message("로그인이 완료되었습니다.")
                .build();
    }
}
