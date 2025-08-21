package site.petful.userservice.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    /** 화이트리스트는 필터를 아예 타지 않게 우회(보안 설정에도 permitAll 넣어두는 걸 권장) */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String p = request.getServletPath();
        return p.startsWith("/api/auth/login")
                || p.startsWith("/api/auth/refresh")
                || p.startsWith("/api/auth/validate-token")
                || p.startsWith("/actuator")
                || p.startsWith("/v3/api-docs")
                || p.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(BEARER.length()).trim();

        try {
            // JwtUtil.extractUsername()은 Access 토큰 기준으로 subject를 꺼내며,
            // typ != access 이거나 서명/만료 문제가 있으면 예외가 나거나 validate에서 false가 됨.
            String username = jwtUtil.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(token, userDetails)) { // typ=access, subject, 만료 검증
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("JWT 인증 성공: {}", username);
                } else {
                    log.debug("JWT 유효성 검증 실패(typ/subject/만료 불일치)");
                }
            }
        } catch (ExpiredJwtException e) {
            log.debug("JWT 만료: {}", e.getMessage());
            // 여기서 바로 401을 내고 끊고 싶다면:
            // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // return;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT 파싱/서명 오류: {}", e.getMessage());
            // 필요 시 401 즉시 응답 처리 가능
        }

        filterChain.doFilter(request, response);
    }
}
