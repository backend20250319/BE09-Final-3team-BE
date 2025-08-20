package site.petful.userservice.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.petful.userservice.domain.User;
import site.petful.userservice.security.CustomUserDetailsService;
import site.petful.userservice.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Value("${jwt.access-exp-min}")   private long accessExpMin;
    @Value("${jwt.refresh-exp-days}") private long refreshExpDays;

    /** 로그인 시 Access/Refresh 각각 생성 (필요한 쪽만 골라 쓰면 됨) */
    public String issueAccess(String username) {
        return jwtUtil.generateAccessToken(username);
    }

    /** User 객체로부터 userNo와 userRole을 포함한 Access 토큰 생성 */
    public String issueAccess(User user) {
        return jwtUtil.generateAccessToken(user);
    }

    /** userNo와 userRole을 직접 지정하여 Access 토큰 생성 */
    public String issueAccess(String username, Long userNo, String userRole) {
        return jwtUtil.generateAccessToken(username, userNo, userRole);
    }

    public String issueRefresh(String username) {
        return jwtUtil.generateRefreshToken(username);
    }

    /** Refresh 토큰으로 새 Access 발급 */
    public String refreshAccess(String refreshToken) {
        Claims claims = jwtUtil.parseRefreshClaims(refreshToken);
        if (jwtUtil.isExpired(claims)) {
            throw new IllegalStateException("refresh token expired");
        }
        String username = claims.getSubject();
        // 유저 유효성 확인(삭제/잠금 등)
        userDetailsService.loadUserByUsername(username);
        return jwtUtil.generateAccessToken(username);
    }

    /** (선택) rolling refresh가 필요할 때만 호출 */
    public String rotateRefresh(String refreshToken) {
        Claims claims = jwtUtil.parseRefreshClaims(refreshToken);
        if (jwtUtil.isExpired(claims)) {
            throw new IllegalStateException("refresh token expired");
        }
        return jwtUtil.generateRefreshToken(claims.getSubject());
    }

    // 컨트롤러에서 쿠키 TTL 설정할 때 사용
    public long accessTtlMinutes() { return accessExpMin; }
    public long refreshTtlDays() { return refreshExpDays; }
}
