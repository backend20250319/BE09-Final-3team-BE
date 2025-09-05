package site.petful.advertiserservice.login.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.advertiserservice.entity.advertiser.Advertiser;
import site.petful.advertiserservice.login.dto.LoginRequest;
import site.petful.advertiserservice.login.dto.LoginResponse;
import site.petful.advertiserservice.login.dto.PasswordResetRequest;
import site.petful.advertiserservice.login.dto.PasswordResetResponse;
import site.petful.advertiserservice.login.dto.PasswordResetChangeRequest;
import site.petful.advertiserservice.login.service.PasswordResetEmailService;
import site.petful.advertiserservice.repository.AdvertiserRepository;
import site.petful.advertiserservice.security.JwtTokenProvider;
import site.petful.advertiserservice.common.ErrorCode;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final AdvertiserRepository advertiserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordResetEmailService passwordResetEmailService;

    public LoginResponse login(LoginRequest request) {
        // 사용자 조회
        Advertiser advertiser = advertiserRepository.findByUserId(request.getUserId())
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

    // 비밀번호 찾기 - 인증 코드 요청
    public PasswordResetResponse requestPasswordReset(PasswordResetRequest request) {
        try {
            log.info("비밀번호 재설정 인증 코드 요청: {}", request.getEmail());
            
            // PasswordResetEmailService를 통해 실제 이메일 발송
            passwordResetEmailService.sendPasswordResetCode(request.getEmail());
            
            log.info("비밀번호 재설정 인증 코드 발송 완료: {}", request.getEmail());
            
            return PasswordResetResponse.builder()
                    .email(request.getEmail())
                    .message("비밀번호 재설정 인증 코드가 이메일로 발송되었습니다.")
                    .build();
        } catch (Exception e) {
            log.error("비밀번호 재설정 인증 코드 발송 실패: {}", e.getMessage());
            throw new RuntimeException("비밀번호 재설정 인증 코드 발송에 실패했습니다: " + e.getMessage());
        }
    }

    // 비밀번호 변경만 수행 (인증코드 확인 후)
    @Transactional
    public PasswordResetResponse changePasswordAfterVerification(PasswordResetChangeRequest request) {
        // 비밀번호 확인
        if (!request.isPasswordMatching()) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 사용자 조회
        Advertiser advertiser = advertiserRepository.findByUserId(request.getEmail())
                .orElseThrow(() -> new RuntimeException(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage()));

        // 계정 활성화 상태 확인
        if (!advertiser.getIsActive()) {
            throw new RuntimeException("비활성화된 계정입니다.");
        }

        // 비밀번호 변경
        advertiser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        advertiserRepository.save(advertiser);

        log.info("비밀번호 변경 완료: {}", request.getEmail());

        return PasswordResetResponse.builder()
                .email(request.getEmail())
                .message("비밀번호가 성공적으로 변경되었습니다.")
                .build();
    }
}
