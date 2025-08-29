package site.petful.userservice.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.petful.userservice.security.JwtUtil;

@Component
public class JwtTokenUtil {
    
    private final JwtUtil jwtUtil;
    
    @Autowired
    public JwtTokenUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * JWT 토큰에서 사용자 번호를 추출
     */
    public Long extractUserNoFromToken(String token) {
        try {
            // "Bearer " 제거
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // JwtUtil을 사용하여 올바른 서명 키로 토큰 파싱 시도
            try {
                return jwtUtil.extractUserNo(token);
            } catch (Exception e) {
                System.out.println("JwtUtil로 토큰 파싱 실패, fallback 시도: " + e.getMessage());
                // JwtUtil 파싱이 실패하면 서명 키 없이 파싱 시도
                return extractUserNoWithoutKey(token);
            }
            
        } catch (Exception e) {
            System.out.println("토큰에서 사용자 번호 추출 실패: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 서명 키 없이 토큰 파싱 (fallback용)
     */
    public Long extractUserNoWithoutKey(String token) {
        try {
            // JWT 토큰 파싱 (서명 검증 없이)
            Claims claims = Jwts.parserBuilder()
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // userNo 추출
            Object userNoObj = claims.get("userNo");
            if (userNoObj != null) {
                if (userNoObj instanceof Integer) {
                    return ((Integer) userNoObj).longValue();
                } else if (userNoObj instanceof Long) {
                    return (Long) userNoObj;
                } else if (userNoObj instanceof String) {
                    return Long.parseLong((String) userNoObj);
                }
            }
            
            return null;
        } catch (Exception e) {
            System.out.println("서명 키 없이 토큰 파싱 실패: " + e.getMessage());
            return null;
        }
    }
}
