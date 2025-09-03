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
}


