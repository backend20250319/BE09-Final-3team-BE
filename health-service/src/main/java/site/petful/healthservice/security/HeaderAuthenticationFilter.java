package site.petful.healthservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader("X-User-No");
        String role = request.getHeader("X-User-Type");

        log.info("[HeaderAuthenticationFilter] userNo : {}, role : {}", userId, role);

        if (userId != null && role != null) {
            // 헤더가 있으면 인증 정보 설정
            PreAuthenticatedAuthenticationToken authentication =
                new PreAuthenticatedAuthenticationToken(userId, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("[HeaderAuthenticationFilter] 인증 성공: userNo={}, role={}", userId, role);
        } else {
            log.warn("[HeaderAuthenticationFilter] 인증 헤더 누락: userNo={}, role={}", userId, role);
        }

        // 항상 다음 필터로 진행 (Spring Security가 인증 상태에 따라 처리)
        filterChain.doFilter(request, response);
    }
}
