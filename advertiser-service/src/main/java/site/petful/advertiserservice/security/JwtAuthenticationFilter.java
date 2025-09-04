package site.petful.advertiserservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String token = extractTokenFromRequest(request);
            
            if (StringUtils.hasText(token)) {
                log.info("JWT 토큰 추출됨: {}", token.substring(0, Math.min(50, token.length())) + "...");
                
                if (jwtTokenProvider.validateAccessToken(token)) {
                    log.info("JWT 토큰 검증 성공");
                    
                    Long userNo = jwtTokenProvider.getUserNoFromAccessToken(token);
                    String userType = jwtTokenProvider.getUserTypeFromAccessToken(token);
                    
                    log.info("토큰에서 추출된 정보 - userNo: {}, userType: {}", userNo, userType);
                    
                    if (userNo != null && userType != null) {
                        // 인증 객체 생성
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userNo,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userType.toUpperCase()))
                        );
                        
                        // SecurityContext에 인증 정보 설정
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("인증 정보 설정 완료 - userNo: {}, authorities: {}", userNo, authentication.getAuthorities());
                    } else {
                        log.warn("토큰에서 userNo 또는 userType을 추출할 수 없음 - userNo: {}, userType: {}", userNo, userType);
                    }
                } else {
                    log.warn("JWT 토큰 검증 실패");
                }
            } else {
                log.debug("요청에 JWT 토큰이 없음");
            }
        } catch (Exception e) {
            log.error("JWT 토큰 처리 중 오류 발생", e);
        }
        
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
