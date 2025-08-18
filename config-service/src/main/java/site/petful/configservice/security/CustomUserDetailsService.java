package site.petful.configservice.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    // 실제 프로젝트에서는 데이터베이스에서 사용자 정보를 조회해야 합니다.
    // 여기서는 예시로 하드코딩된 사용자를 사용합니다.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 예시 사용자 - 실제로는 데이터베이스에서 조회
        if ("admin".equals(username)) {
            return new User("admin", 
                    "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG", // "password"의 BCrypt 해시
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        } else if ("user".equals(username)) {
            return new User("user", 
                    "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG", // "password"의 BCrypt 해시
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        }
        
        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}
