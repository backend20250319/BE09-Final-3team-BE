package site.petful.gatewayservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.petful.gatewayservice.config.JwtConfig;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private static final String CLAIM_USER_NO = "userNo";
    private static final String CLAIM_USER_TYPE = "userType";

    private final JwtConfig jwtConfig;

    /** 파서 공통 로직 */
    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        // 만약 secret이 "Base64로 인코딩된 값"이라면 위 줄 대신:
        // SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.getSecret()));
        return Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token); // 파싱만 성공해도 서명 검증/구조 검증 통과
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            Date exp = claims.getExpiration();
            return exp == null || exp.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /** userNo 추출 */
    public String getUserNoFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            Object v = claims.get(CLAIM_USER_NO);
            return v == null ? null : String.valueOf(v);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error extracting userNo from token: {}", e.getMessage());
            return null;
        }
    }

    /** userType 추출 */
    public String getUserTypeFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            Object v = claims.get(CLAIM_USER_TYPE);
            return v == null ? null : String.valueOf(v);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error extracting userType from token: {}", e.getMessage());
            return null;
        }
    }

    /** 하위 호환: 기존 호출부가 있을 수 있으므로 남겨둠 */
    @Deprecated
    public String getUserIdFromToken(String token) {
        return getUserNoFromToken(token);
    }
}
