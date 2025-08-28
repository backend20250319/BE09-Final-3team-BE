package site.petful.advertiserservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.access-secret:default-access-secret-key-for-development-only-change-in-production}")
    private String accessSecret;

    @Value("${jwt.refresh-secret:default-refresh-secret-key-for-development-only-change-in-production}")
    private String refreshSecret;

    @Value("${jwt.access-exp-min:60}")
    private long accessExpMin;

    @Value("${jwt.refresh-exp-days:14}")
    private long refreshExpDays;

    private SecretKey getAccessSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret.trim()));
    }

    private SecretKey getRefreshSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret.trim()));
    }

    public String generateAccessToken(Long advertiserNo, String userType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (accessExpMin * 60 * 1000));

        return Jwts.builder()
                .claim("advertiserNo", advertiserNo)
                .claim("userType", userType)
                .claim("tokenType", "ACCESS")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getAccessSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long advertiserNo, String userType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (refreshExpDays * 24 * 60 * 60 * 1000));

        return Jwts.builder()
                .claim("advertiserNo", advertiserNo)
                .claim("userType", userType)
                .claim("tokenType", "REFRESH")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getRefreshSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getAccessTokenClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getAccessSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims getRefreshTokenClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getRefreshSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getAdvertiserNoFromAccessToken(String token) {
        Claims claims = getAccessTokenClaims(token);
        return claims.get("advertiserNo", Long.class);
    }

    public Long getAdvertiserNoFromRefreshToken(String token) {
        Claims claims = getRefreshTokenClaims(token);
        return claims.get("advertiserNo", Long.class);
    }

    public String getUserTypeFromAccessToken(String token) {
        Claims claims = getAccessTokenClaims(token);
        return claims.get("userType", String.class);
    }

    public String getUserTypeFromRefreshToken(String token) {
        Claims claims = getRefreshTokenClaims(token);
        return claims.get("userType", String.class);
    }

    // userNo 또는 advertiserNo를 반환하는 통합 메서드
    public Long getUserNoFromAccessToken(String token) {
        Claims claims = getAccessTokenClaims(token);
        Long userNo = claims.get("userNo", Long.class);
        Long advertiserNo = claims.get("advertiserNo", Long.class);
        return userNo != null ? userNo : advertiserNo;
    }

    public boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getAccessSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getRefreshSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
