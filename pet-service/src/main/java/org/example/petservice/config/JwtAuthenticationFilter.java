package org.example.petservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.access-secret}")
    private String accessSecret;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        log.debug("JWT Filter - Request URI: {}", request.getRequestURI());
        
        String token = extractTokenFromRequest(request);
        log.debug("JWT Filter - Extracted token: {}", token != null ? "present" : "null");
        
        if (StringUtils.hasText(token) && validateToken(token)) {
            log.debug("JWT Filter - Token is valid");
            Claims claims = extractClaims(token);
            // User Service JWT 구조에 맞춰 userNo와 userType 추출
            Long userNo = claims.get("userNo", Long.class);
            String userType = claims.get("userType", String.class);
            log.debug("JWT Filter - Extracted userNo: {}, userType: {}", userNo, userType);
            
            // JWT 토큰에서 추출한 userNo를 RequestAttribute에 설정
            request.setAttribute("X-User-No", userNo);
            
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userNo, null, List.of(new SimpleGrantedAuthority("ROLE_" + userType))
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 수정된 요청으로 계속 진행
            log.debug("JWT Filter - Setting X-User-No attribute: {}", userNo);
            filterChain.doFilter(request, response);
            return;
        }
        
        log.debug("JWT Filter - No valid token found, proceeding without authentication");
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            log.debug("JWT Filter - Validating token with secret: {}", accessSecret != null ? "present" : "null");
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret.trim())))
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("JWT Filter - Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret.trim())))
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
