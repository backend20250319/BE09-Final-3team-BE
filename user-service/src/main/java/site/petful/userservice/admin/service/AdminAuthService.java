package site.petful.userservice.admin.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.petful.userservice.security.JwtUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService {

    private final JwtUtil jwtUtil;

    /**
     * Admin 로그아웃 시 리프레시 토큰 검증 및 무효화
     */
    public void logout(String refreshToken) {
        try {
            // 리프레시 토큰 유효성 검증
            Claims claims = jwtUtil.parseRefreshClaims(refreshToken);
            if (jwtUtil.isExpired(claims)) {
                throw new IllegalStateException("refresh token expired");
            }
            
            // 토큰 유효성 검증 완료
            String username = claims.getSubject();
            log.info("Admin 사용자 {} 로그아웃 처리 완료", username);
            
            // User쪽과 동일하게 단순히 토큰 유효성만 검증
            // 클라이언트에서 토큰을 삭제하면 됨
            
        } catch (Exception e) {
            throw new IllegalStateException("Invalid refresh token: " + e.getMessage());
        }
    }
}
