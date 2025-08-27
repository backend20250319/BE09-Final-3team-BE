package site.petful.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import site.petful.userservice.entity.User;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final String CLAIM_TYP = "typ";
    private static final String TYP_ACCESS = "access";
    private static final String TYP_REFRESH = "refresh";
    private static final String CLAIM_USER_NO = "userNo";
    private static final String CLAIM_USER_TYPE = "userType";

    // application.yml
    @Value("${jwt.access-secret}")
    private String accessSecret;

    @Value("${jwt.refresh-secret}")
    private String refreshSecret;

    @Value("${jwt.access-exp-min}")
    private long accessExpMin;

    @Value("${jwt.refresh-exp-days}")
    private long refreshExpDays;

    /* ======================
     * Key helpers (jjwt 0.11.x)
     * ====================== */
    private Key accessKey()  {
        // Base64 인코딩된 시크릿을 사용한다고 가정
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret.trim()));
    }
    private Key refreshKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret.trim()));
    }

    /* ======================
     * Access token (인증용)
     * ====================== */

    /** userNo와 userType을 포함한 Access 토큰 생성 */
    public String generateAccessToken(String subject, Long userNo, String userType) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .claim(CLAIM_TYP, TYP_ACCESS)
                .claim(CLAIM_USER_NO, userNo)
                .claim(CLAIM_USER_TYPE, userType)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + Duration.ofMinutes(accessExpMin).toMillis()))
                .signWith(accessKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** User 객체로부터 Access 토큰 생성 (래퍼) */
    public String generateAccessToken(User user) {
        return generateAccessToken(
                user.getEmail(),
                user.getUserNo(),
                user.getUserType().name()
        );
    }

    /** Access 파싱 (+ typ 검증) */
    public Claims parseAccessClaims(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(accessKey())
                .build()
                .parseClaimsJws(token);
        Claims claims = jws.getBody();
        if (!TYP_ACCESS.equals(claims.get(CLAIM_TYP))) {
            throw new IllegalArgumentException("Not an access token");
        }
        return claims;
    }

    /* ======================
     * Refresh token (갱신용)
     * ====================== */
    public String generateRefreshToken(String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .claim(CLAIM_TYP, TYP_REFRESH)
                .setId(UUID.randomUUID().toString()) // jti
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + Duration.ofDays(refreshExpDays).toMillis()))
                .signWith(refreshKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims parseRefreshClaims(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(refreshKey())
                .build()
                .parseClaimsJws(token);
        Claims c = jws.getBody();
        if (!TYP_REFRESH.equals(c.get(CLAIM_TYP))) {
            throw new IllegalArgumentException("Not a refresh token");
        }
        return c;
    }

    /* ======================
     * 공통/호환 메서드들 (Access 기준)
     * ====================== */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /** 토큰에서 userNo 추출 (Number/String 모두 안전 변환) */
    public Long extractUserNo(String token) {
        return extractClaim(token, claims -> {
            Object v = claims.get(CLAIM_USER_NO);
            if (v == null) return null;
            if (v instanceof Number) return ((Number) v).longValue();
            return Long.parseLong(v.toString());
        });
    }

    /** 토큰에서 userType 추출 */
    public String extractUserType(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_USER_TYPE, String.class));
    }

    /** 과거 명칭 유지(내부적으로 userType을 반환) */
    @Deprecated
    public String extractUserRole(String token) {
        return extractUserType(token);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseAccessClaims(token);
        return claimsResolver.apply(claims);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /** 사용자명 검증이 필요한 경우 */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            Claims c = parseAccessClaims(token);
            boolean typeOk = TYP_ACCESS.equals(c.get(CLAIM_TYP));
            String username = c.getSubject();
            return typeOk && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /** 액세스 토큰 기본 검증(서명/만료/typ) */
    public boolean validateAccessToken(String token) {
        try {
            Claims c = parseAccessClaims(token);
            return !isExpired(c);
        } catch (Exception e) {
            return false;
        }
    }

    /** 리프레시 토큰 기본 검증(서명/만료/typ) */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims c = parseRefreshClaims(refreshToken);
            return !isExpired(c);
        } catch (Exception e) {
            return false;
        }
    }

    // 필요 시 외부에서 만료 체크/subject 추출
    public boolean isExpired(Claims claims) {
        Date exp = claims.getExpiration();
        return exp == null || exp.before(new Date());
    }
    public String extractUsernameFromRefresh(String refreshToken) {
        return parseRefreshClaims(refreshToken).getSubject();
    }
    public Date extractExpirationFromRefresh(String refreshToken) {
        return parseRefreshClaims(refreshToken).getExpiration();
    }
}
