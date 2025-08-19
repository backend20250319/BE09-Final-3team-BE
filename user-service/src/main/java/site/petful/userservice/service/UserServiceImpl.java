package site.petful.userservice.service;

import site.petful.userservice.domain.Role;
import site.petful.userservice.domain.User;
import site.petful.userservice.dto.AuthResponse;
import site.petful.userservice.dto.LoginRequest;
import site.petful.userservice.dto.SignupRequest;
import site.petful.userservice.dto.TokenValidationResponse;
import site.petful.userservice.repository.UserRepository;
import site.petful.userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Override
    public AuthResponse signup(SignupRequest request) {
        // 입력 데이터 검증
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("이메일은 필수 입력 항목입니다.");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("비밀번호는 필수 입력 항목입니다.");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("이름은 필수 입력 항목입니다.");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new RuntimeException("전화번호는 필수 입력 항목입니다.");
        }
        
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 새 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .address(request.getAddress())
                .detailedAddress(request.getDetailedAddress())
                .birthYear(request.getBirthYear())
                .birthMonth(request.getBirthMonth())
                .birthDay(request.getBirthDay())
                .emailVerified(false) // 이메일 인증은 별도로 처리
                .role(Role.User) // 기본 역할은 User
                .build();

        User savedUser = userRepository.save(user);
        
        // 회원가입 성공 로그
        System.out.println("✅ 회원가입 성공: " + savedUser.getEmail() + " (ID: " + savedUser.getId() + ")");

        return AuthResponse.builder()
                .message("회원가입이 성공적으로 완료되었습니다. 로그인 후 서비스를 이용하세요.")
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            // 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // JWT 토큰 생성
            String token = jwtUtil.generateToken(userDetails);

            return AuthResponse.builder()
                    .token(token)
                    .message("로그인이 성공적으로 완료되었습니다.")
                    .email(user.getEmail())
                    .name(user.getName())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    @Override
    public void markEmailVerified(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        try {
            String userEmail = jwtUtil.extractUsername(token);
            if (userEmail != null) {
                User userDetails = userRepository.findByEmail(userEmail)
                        .orElse(null);
                
                if (userDetails != null && jwtUtil.validateToken(token, userDetails)) {
                    User user = userDetails;
                    return TokenValidationResponse.builder()
                            .valid(true)
                            .email(user.getEmail())
                            .name(user.getName())
                            .role(user.getRole().name())
                            .message("토큰이 유효합니다.")
                            .build();
                }
            }
            
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("토큰이 유효하지 않습니다.")
                    .build();
                    
        } catch (Exception e) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("토큰 검증 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
        }
    }
}
