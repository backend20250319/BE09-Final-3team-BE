package site.petful.advertiserservice.login.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import site.petful.advertiserservice.entity.advertiser.Advertiser;
import site.petful.advertiserservice.login.dto.LoginRequest;
import site.petful.advertiserservice.login.dto.LoginResponse;
import site.petful.advertiserservice.login.dto.PasswordChangeRequest;
import site.petful.advertiserservice.login.dto.PasswordResetRequest;
import site.petful.advertiserservice.login.dto.PasswordResetConfirmRequest;
import site.petful.advertiserservice.login.dto.PasswordResetResponse;
import site.petful.advertiserservice.repository.AdvertiserRepository;
import site.petful.advertiserservice.security.JwtTokenProvider;
import site.petful.advertiserservice.common.ErrorCode;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AdvertiserRepository advertiserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

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

    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        // 비밀번호 확인
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 사용자 조회
        Advertiser advertiser = advertiserRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage()));

        // 비밀번호 변경
        advertiser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        advertiserRepository.save(advertiser);
    }

    // 비밀번호 찾기 - 인증 코드 요청
    public PasswordResetResponse requestPasswordReset(PasswordResetRequest request) {
        // 사용자 존재 여부 확인
        Advertiser advertiser = advertiserRepository.findByUserId(request.getEmail())
                .orElseThrow(() -> new RuntimeException(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage()));

        // 계정 활성화 상태 확인
        if (!advertiser.getIsActive()) {
            throw new RuntimeException("비활성화된 계정입니다.");
        }

        // 인증 코드 생성 (6자리 랜덤 숫자)
        String verificationCode = generateVerificationCode();
        
        // Redis에 인증 코드 저장 (5분 유효)
        String codeKey = "password_reset_code:" + request.getEmail();
        String attemptsKey = "password_reset_attempts:" + request.getEmail();
        
        redisTemplate.opsForValue().set(codeKey, verificationCode, Duration.ofSeconds(300)); // 5분
        redisTemplate.opsForValue().set(attemptsKey, "0", Duration.ofSeconds(300)); // 5분
        
        // TODO: 이메일 발송 서비스 구현 필요
        // emailService.sendPasswordResetEmail(request.getEmail(), verificationCode);

        return PasswordResetResponse.builder()
                .email(request.getEmail())
                .message("비밀번호 재설정 인증 코드가 이메일로 발송되었습니다.")
                .build();
    }

    // 비밀번호 재설정 확인
    @Transactional
    public PasswordResetResponse confirmPasswordReset(PasswordResetConfirmRequest request) {
        // 비밀번호 확인
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 사용자 조회
        Advertiser advertiser = advertiserRepository.findByUserId(request.getEmail())
                .orElseThrow(() -> new RuntimeException(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage()));

        // Redis에서 인증 코드 검증
        String codeKey = "password_reset_code:" + request.getEmail();
        String attemptsKey = "password_reset_attempts:" + request.getEmail();
        
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        if (storedCode == null) {
            throw new RuntimeException("인증 코드가 만료되었습니다. 다시 요청해주세요.");
        }

        // 시도 횟수 확인
        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
        if (attempts >= 5) {
            // 초과 시 정리 후 실패
            redisTemplate.delete(codeKey);
            redisTemplate.delete(attemptsKey);
            throw new RuntimeException("인증 시도 횟수를 초과했습니다. 다시 요청해주세요.");
        }

        // 코드 일치 확인
        if (!storedCode.equals(request.getVerificationCode())) {
            // 불일치 시 실패 카운트 +1
            redisTemplate.opsForValue().increment(attemptsKey);
            throw new RuntimeException("인증 코드가 올바르지 않습니다.");
        }

        // 비밀번호 변경
        advertiser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        advertiserRepository.save(advertiser);

        // Redis에서 인증 코드 삭제
        redisTemplate.delete(codeKey);
        redisTemplate.delete(attemptsKey);

        return PasswordResetResponse.builder()
                .email(request.getEmail())
                .message("비밀번호가 성공적으로 변경되었습니다.")
                .build();
    }

    // 6자리 랜덤 인증 코드 생성
    private String generateVerificationCode() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }
}
