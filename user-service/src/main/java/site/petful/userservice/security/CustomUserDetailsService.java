package site.petful.userservice.security;

import site.petful.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    /**
     * 사용자 번호로 UserDetails를 로드합니다.
     * @param userNo 사용자 번호
     * @return UserDetails 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    public UserDetails loadUserById(Long userNo) throws UsernameNotFoundException {
        return userRepository.findById(userNo)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: userNo=" + userNo));
    }
}
