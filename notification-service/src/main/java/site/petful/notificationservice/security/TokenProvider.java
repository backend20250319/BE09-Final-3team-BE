package site.petful.notificationservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class TokenProvider {

    private final SecretKey key;

    public TokenProvider(@Value("${jwt.access-secret}") String accessSecret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret.trim()));
    }

    /** "Authorization: Bearer xxx.yyy.zzz"에서 userNo 추출 (없으면 null) */
    public Long resolveUserNoFromAuthHeader(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) return null;
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            Claims c = Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody();
            Number n = c.get("userNo", Number.class);
            return (n != null) ? n.longValue() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
