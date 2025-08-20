package site.petful.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import io.jsonwebtoken.io.Decoders;
import site.petful.userservice.domain.User;

@Component
public class JwtUtil {

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
     * Key helpers (0.11.5)
     * ====================== */
    private Key accessKey()  {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret.trim()));  // ← BASE64
    }
    private Key refreshKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret.trim())); // ← BASE64
    }



    /* ======================
     * Access token (인증용)
     * ====================== */
    public String generateAccessToken(String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .claim("typ", "access")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + Duration.ofMinutes(accessExpMin).toMillis()))
                .signWith(accessKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(String subject, Map<String, Object> extraClaims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .claim("typ", "access")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + Duration.ofMinutes(accessExpMin).toMillis()))
                .signWith(accessKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** userNo와 userRole을 포함한 Access 토큰 생성 */
    public String generateAccessToken(String subject, Long userNo, String userRole) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .claim("typ", "access")
                .claim("userNo", userNo)
                .claim("userRole", userRole)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + Duration.ofMinutes(accessExpMin).toMillis()))
                .signWith(accessKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** User 객체로부터 Access 토큰 생성 */
    public String generateAccessToken(site.petful.userservice.domain.User user) {
        return generateAccessToken(user.getEmail(), user.getUserNo(), user.getUserType().name());
    }

    /** 기존 호환: UserDetails로 Access 발급 */
    public String generateToken(UserDetails userDetails) {
        return generateAccessToken(userDetails.getUsername());
    }

    /** Access 파싱 */
    public Claims parseAccessClaims(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(accessKey())
                .build()
                .parseClaimsJws(token);
        return jws.getBody();
    }

    /* ======================
     * Refresh token (갱신용)
     * ====================== */
    public String generateRefreshToken(String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .claim("typ", "refresh")
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
        if (!"refresh".equals(c.get("typ"))) {
            throw new IllegalArgumentException("Not a refresh token");
        }
        return c;
    }

    /* ======================
     * 공통/호환 메서드들
     * ====================== */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject); // Access 기준
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration); // Access 기준
    }

    /** 토큰에서 userNo 추출 */
    public Long extractUserNo(String token) {
        return extractClaim(token, claims -> claims.get("userNo", Long.class));
    }

    /** 토큰에서 userRole 추출 */
    public String extractUserRole(String token) {
        return extractClaim(token, claims -> claims.get("userRole", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseAccessClaims(token);
        return claimsResolver.apply(claims);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            Claims c = parseAccessClaims(token);
            boolean typeOk = "access".equals(c.get("typ"));
            String username = c.getSubject();
            return typeOk && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
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
