package site.petful.advertiserservice.signup.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.entity.advertiser.Advertiser;
import site.petful.advertiserservice.repository.AdvertiserRepository;
import site.petful.advertiserservice.signup.dto.EmailVerificationConfirmRequest;
import site.petful.advertiserservice.signup.dto.SignupRequest;
import site.petful.advertiserservice.signup.dto.SignupResponse;
import site.petful.advertiserservice.signup.dto.WithdrawRequest;
import site.petful.advertiserservice.signup.dto.WithdrawResponse;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final AdvertiserRepository advertiserRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

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
        if (advertiserRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException(ErrorCode.EMAIL_ALREADY_EXISTS.getDefaultMessage());
        }

        // 광고주 엔티티 생성
        Advertiser advertiser = new Advertiser();
        advertiser.setUserId(request.getUserId());
        advertiser.setPassword(passwordEncoder.encode(request.getPassword()));
        advertiser.setName(request.getName());
        advertiser.setPhone(request.getPhone());
        advertiser.setBusinessNumber(request.getBusinessNumber());
        advertiser.setIsActive(true);
        advertiser.setIsApproved(false);

        Advertiser savedAdvertiser = advertiserRepository.save(advertiser);

        return SignupResponse.builder()
                .advertiserNo(savedAdvertiser.getAdvertiserNo())
                .userType("ADVERTISER")
                .message("회원가입이 완료되었습니다. 로그인 후 서비스를 이용해주세요.")
                .build();
    }

    // 광고주 회원탈퇴
    @Transactional
    public WithdrawResponse withdraw(Long advertiserNo, WithdrawRequest request) {
        // 광고주 조회
        Advertiser advertiser = advertiserRepository.findByAdvertiserNo(advertiserNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage()));
        
        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), advertiser.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        
        // 이미 탈퇴한 광고주인지 확인
        if (!advertiser.getIsActive()) {
            throw new IllegalStateException("이미 탈퇴한 계정입니다.");
        }
        
        // 탈퇴 정보 저장 (로그 목적)
        String withdrawInfo = "탈퇴 사유: " + request.getReason() + " (탈퇴일: " + java.time.LocalDateTime.now() + ")";
        // 로그 출력 (실제 로깅 라이브러리 사용 시)
        System.out.println("회원탈퇴 - 광고주: " + advertiser.getUserId() + ", 사유: " + request.getReason());
        
        // Soft Delete: isActive를 false로 설정하고 탈퇴 사유 저장
        advertiser.suspend(); // isActive = false
        advertiser.setReason(request.getReason());
        
        // 광고주 정보 업데이트
        advertiserRepository.save(advertiser);
        
        return WithdrawResponse.builder()
                .advertiserNo(advertiser.getAdvertiserNo())
                .email(advertiser.getUserId())
                .message("회원탈퇴가 성공적으로 처리되었습니다.")
                .withdrawnAt(java.time.LocalDateTime.now())
                .build();
    }
}



