package site.petful.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import site.petful.userservice.domain.Role;
import site.petful.userservice.domain.User;
import site.petful.userservice.dto.SignupRequest;
import site.petful.userservice.dto.SignupResponse;
import site.petful.userservice.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SignupResponse signup(SignupRequest request) {
        // 입력 검증
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수 입력 항목입니다.");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수 입력 항목입니다.");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수 입력 항목입니다.");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("전화번호는 필수 입력 항목입니다.");
        }

        // 이메일 중복
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
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
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }
}
