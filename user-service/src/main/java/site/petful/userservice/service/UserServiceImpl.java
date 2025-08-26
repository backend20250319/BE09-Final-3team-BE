package site.petful.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.userservice.common.ErrorCode;
import site.petful.userservice.domain.Role;
import site.petful.userservice.domain.User;
import site.petful.userservice.domain.UserProfile;
import site.petful.userservice.dto.PasswordChangeRequest;
import site.petful.userservice.dto.PasswordResetRequest;
import site.petful.userservice.dto.PasswordResetResponse;
import site.petful.userservice.dto.VerificationConfirmRequest;
import site.petful.userservice.dto.VerificationConfirmResponse;
import site.petful.userservice.dto.ProfileResponse;
import site.petful.userservice.dto.ProfileUpdateRequest;
import site.petful.userservice.dto.SignupRequest;
import site.petful.userservice.dto.SignupResponse;
import site.petful.userservice.repository.UserProfileRepository;
import site.petful.userservice.repository.UserRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisService redisService;

    @Override
    public SignupResponse signup(SignupRequest request) {
        // 입력 검증
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST.getDefaultMessage() + " - 이메일은 필수 입력 항목입니다.");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST.getDefaultMessage() + " - 비밀번호는 필수 입력 항목입니다.");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST.getDefaultMessage() + " - 이름은 필수 입력 항목입니다.");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST.getDefaultMessage() + " - 전화번호는 필수 입력 항목입니다.");
        }

        // 이메일 중복
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException(ErrorCode.DUPLICATE_EMAIL.getDefaultMessage());
        }

        // 사용자 저장
        User saved = userRepository.save(User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .userType(request.getUserType() != null ? request.getUserType() : Role.User) // 기본값 설정
                .birthDate(request.getBirthDate())
                .description(request.getDescription())
                .roadAddress(request.getRoadAddress())
                .detailAddress(request.getDetailAddress())
                // 기존 필드들 (호환성을 위해 유지)
                .address(request.getAddress())
                .detailedAddress(request.getDetailedAddress())
                .birthYear(request.getBirthYear())
                .birthMonth(request.getBirthMonth())
                .birthDay(request.getBirthDay())
                .emailVerified(false)
                .isActive(true)
                .build());

        return SignupResponse.builder()
                .userNo(saved.getUserNo())
                .email(saved.getEmail())
                .name(saved.getName())
                .message("회원가입이 성공적으로 완료되었습니다. 로그인 후 이용하세요.")
                .build();
    }

    @Override
    public void markEmailVerified(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(ErrorCode.USER_NOT_FOUND.getDefaultMessage()));
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(ErrorCode.USER_NOT_FOUND.getDefaultMessage()));
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userNo) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.USER_NOT_FOUND.getDefaultMessage()));
        
        UserProfile profile = userProfileRepository.findByUser_UserNo(userNo)
                .orElse(null);
        
        // 프로필이 없으면 기본 사용자 정보에서 생년월일 가져오기
        LocalDate birthDate = null;
        if (profile != null && profile.getBirthDate() != null) {
            birthDate = profile.getBirthDate();
        } else if (user.getBirthDate() != null) {
            birthDate = user.getBirthDate();
        }
        
        return ProfileResponse.builder()
                .userNo(user.getUserNo())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .role(user.getUserType().name())
                .profileImageUrl(profile != null ? profile.getProfileImageUrl() : null)
                .selfIntroduction(profile != null ? profile.getSelfIntroduction() : null)
                .birthDate(birthDate)
                .roadAddress(profile != null ? profile.getRoadAddress() : null)
                .detailAddress(profile != null ? profile.getDetailAddress() : null)
                .instagramAccount(profile != null ? profile.getInstagramAccount() : null)
                .build();
    }
    
    @Override
    @Transactional
    public ProfileResponse updateProfile(Long userNo, ProfileUpdateRequest request) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.USER_NOT_FOUND.getDefaultMessage()));
        
        UserProfile profile = userProfileRepository.findByUser_UserNo(userNo)
                .orElse(UserProfile.builder()
                        .user(user)
                        .build());
        
        // 프로필 정보 업데이트
        if (request.getProfileImageUrl() != null) {
            profile.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getSelfIntroduction() != null) {
            profile.setSelfIntroduction(request.getSelfIntroduction());
        }
        if (request.getBirthDate() != null) {
            profile.setBirthDate(request.getBirthDate());
        } else if (profile.getBirthDate() == null && user.getBirthDate() != null) {
            // 프로필에 생년월일이 없고 사용자 기본 정보에 있으면 가져오기
            profile.setBirthDate(user.getBirthDate());
        }
        if (request.getRoadAddress() != null) {
            profile.setRoadAddress(request.getRoadAddress());
        }
        if (request.getDetailAddress() != null) {
            profile.setDetailAddress(request.getDetailAddress());
        }
        if (request.getInstagramAccount() != null) {
            profile.setInstagramAccount(request.getInstagramAccount());
        }
        
        UserProfile savedProfile = userProfileRepository.save(profile);
        
        return ProfileResponse.builder()
                .userNo(user.getUserNo())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .role(user.getUserType().name())
                .profileImageUrl(savedProfile.getProfileImageUrl())
                .selfIntroduction(savedProfile.getSelfIntroduction())
                .birthDate(savedProfile.getBirthDate())
                .roadAddress(savedProfile.getRoadAddress())
                .detailAddress(savedProfile.getDetailAddress())
                .instagramAccount(savedProfile.getInstagramAccount())
                .profileUpdatedAt(savedProfile.getUpdatedAt())
                .build();
    }
    
    @Override
    @Transactional
    public PasswordResetResponse requestPasswordReset(PasswordResetRequest request) {
        // 사용자 존재 여부 확인
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException(ErrorCode.USER_NOT_FOUND.getDefaultMessage()));
        
        // 비밀번호 재설정용 인증 코드 생성 (6자리 숫자)
        String verificationCode = generateVerificationCode();
        
        // Redis 연결 테스트
        boolean redisConnected = redisService.testConnection();
        System.out.println("Redis 연결 상태: " + redisConnected);
        
        // Redis에 인증 코드 저장 (10분 유효)
        String redisKey = "password_reset:" + request.getEmail();
        redisService.setValue(redisKey, verificationCode, 600); // 10분 = 600초
        
        // 디버깅을 위한 로그 추가
        System.out.println("=== 비밀번호 재설정 디버깅 ===");
        System.out.println("이메일: " + request.getEmail());
        System.out.println("생성된 인증 코드: " + verificationCode);
        System.out.println("Redis 키: " + redisKey);
        
        // Redis에 저장된 값 확인
        String savedCode = redisService.getValue(redisKey);
        System.out.println("Redis에서 조회한 코드: " + savedCode);
        System.out.println("저장 성공 여부: " + (savedCode != null && savedCode.equals(verificationCode)));
        System.out.println("================================");
        
        // 이메일 발송
        String subject = "[Petful] 비밀번호 재설정 인증 코드";
        String content = String.format(
            "안녕하세요, %s님!\n\n" +
            "비밀번호 재설정을 요청하셨습니다.\n\n" +
            "인증 코드: %s\n\n" +
            "이 인증 코드는 10분간 유효합니다.\n" +
            "본인이 요청하지 않았다면 이 이메일을 무시하세요.\n\n" +
            "감사합니다.\n" +
            "Petful 팀",
            user.getName(),
            verificationCode
        );
        
        emailService.sendEmail(request.getEmail(), subject, content);
        
        return PasswordResetResponse.builder()
                .message("비밀번호 재설정 인증 코드가 이메일로 발송되었습니다.")
                .email(request.getEmail())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public VerificationConfirmResponse verifyPasswordResetCode(VerificationConfirmRequest request) {
        // 인증 코드 검증
        String redisKey = "password_reset:" + request.getEmail();
        String storedCode = redisService.getValue(redisKey);
        
        // 디버깅을 위한 로그 추가
        System.out.println("=== 인증 코드 확인 디버깅 ===");
        System.out.println("요청 이메일: " + request.getEmail());
        System.out.println("요청 인증 코드: " + request.getCode());
        System.out.println("Redis 키: " + redisKey);
        System.out.println("Redis에서 조회한 코드: " + storedCode);
        System.out.println("코드 일치 여부: " + (storedCode != null && storedCode.equals(request.getCode())));
        System.out.println("=====================================");
        
        if (storedCode == null) {
            return VerificationConfirmResponse.builder()
                    .message("인증 코드가 만료되었습니다.")
                    .email(request.getEmail())
                    .verified(false)
                    .build();
        }
        
        if (!storedCode.equals(request.getCode())) {
            return VerificationConfirmResponse.builder()
                    .message("유효하지 않은 인증 코드입니다.")
                    .email(request.getEmail())
                    .verified(false)
                    .build();
        }
        
        return VerificationConfirmResponse.builder()
                .message("인증이 성공적으로 완료되었습니다.")
                .email(request.getEmail())
                .verified(true)
                .build();
    }
    
    @Override
    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        // 비밀번호 확인
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        
        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException(ErrorCode.USER_NOT_FOUND.getDefaultMessage()));
        
        // 비밀번호 변경
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Redis에서 인증 코드 삭제 (선택사항 - 보안을 위해 삭제)
        String redisKey = "password_reset:" + request.getEmail();
        redisService.deleteValue(redisKey);
    }
    
    /**
     * 6자리 숫자 인증 코드 생성
     */
    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}
