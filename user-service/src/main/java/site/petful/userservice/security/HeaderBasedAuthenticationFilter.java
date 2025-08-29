package site.petful.userservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import site.petful.userservice.common.UserHeaderUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class HeaderBasedAuthenticationFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;

    /** 화이트리스트는 필터를 아예 타지 않게 우회 */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.startsWith("/auth/login")
                || path.startsWith("/auth/refresh")
                || path.startsWith("/auth/validate-token")
                || path.startsWith("/auth/signup")
                || path.startsWith("/auth/password/reset")
                || path.startsWith("/auth/password/verify")
                || path.startsWith("/auth/password/change")
                || path.startsWith("/actuator")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // X-User-No 헤더에서 사용자 번호 추출
            Long userNo = UserHeaderUtil.getCurrentUserNo();
            
            if (userNo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 사용자 번호가 있으면 인증 처리
                UserDetails userDetails = this.userDetailsService.loadUserById(userNo);
                
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("헤더 기반 인증 성공: userNo={}", userNo);
                } else {
                    log.warn("헤더 기반 인증 실패: userNo={}에 해당하는 사용자를 찾을 수 없습니다.", userNo);
                }
            }
        } catch (Exception e) {
            log.warn("헤더 기반 인증 처리 중 오류 발생: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
