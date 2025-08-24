package site.petful.advertiserservice.security;

import io.jsonwebtoken.*;
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
        return Keys.hmacShaKeyFor(accessSecret.getBytes());
    }

    private SecretKey getRefreshSigningKey() {
        return Keys.hmacShaKeyFor(refreshSecret.getBytes());
    }

    public String generateAccessToken(Long userNo, String userType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (accessExpMin * 60 * 1000));

        return Jwts.builder()
                .setSubject(String.valueOf(userNo))
                .claim("userType", userType)
                .claim("tokenType", "ACCESS")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getAccessSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userNo, String userType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (refreshExpDays * 24 * 60 * 60 * 1000));

        return Jwts.builder()
                .setSubject(String.valueOf(userNo))
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

    public Long getUserNoFromAccessToken(String token) {
        Claims claims = getAccessTokenClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public Long getUserNoFromRefreshToken(String token) {
        Claims claims = getRefreshTokenClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getUserTypeFromAccessToken(String token) {
        Claims claims = getAccessTokenClaims(token);
        return claims.get("userType", String.class);
    }

    public String getUserTypeFromRefreshToken(String token) {
        Claims claims = getRefreshTokenClaims(token);
        return claims.get("userType", String.class);
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
