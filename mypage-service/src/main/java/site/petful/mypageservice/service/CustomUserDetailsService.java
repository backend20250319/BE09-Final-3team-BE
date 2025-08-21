package site.petful.mypageservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import site.petful.mypageservice.entity.UserBasic;
import site.petful.mypageservice.repository.UserBasicRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserBasicRepository userBasicRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            UserBasic userBasic = userBasicRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
            
            return User.builder()
                    .username(userBasic.getEmail())
                    .password("") // JWT 토큰 기반이므로 비밀번호는 사용하지 않음
                    .authorities("ROLE_USER") // 기본 권한
                    .accountExpired(!userBasic.getIsActive())
                    .accountLocked(!userBasic.getIsActive())
                    .credentialsExpired(false)
                    .disabled(!userBasic.getIsActive())
                    .build();
        } catch (Exception e) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
        }
    }
}
