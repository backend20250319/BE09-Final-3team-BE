package site.petful.userservice;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.petful.userservice.domain.Role;
import site.petful.userservice.domain.User;
import site.petful.userservice.security.JwtUtil;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testGenerateTokenWithUserNoAndUserRole() {
        // Given
        String email = "test@example.com";
        Long userNo = 123L;
        String userRole = "INFLUENCER";

        // When
        String token = jwtUtil.generateAccessToken(email, userNo, userRole);

        // Then
        assertNotNull(token);
        
        // 토큰에서 정보 추출
        String extractedEmail = jwtUtil.extractUsername(token);
        Long extractedUserNo = jwtUtil.extractUserNo(token);
        String extractedUserRole = jwtUtil.extractUserRole(token);

        assertEquals(email, extractedEmail);
        assertEquals(userNo, extractedUserNo);
        assertEquals(userRole, extractedUserRole);
    }

    @Test
    void testGenerateTokenFromUserObject() {
        // Given
        User user = User.builder()
                .userNo(456L)
                .email("admin@example.com")
                .userType(Role.ADMIN)
                .build();

        // When
        String token = jwtUtil.generateAccessToken(user);

        // Then
        assertNotNull(token);
        
        // 토큰에서 정보 추출
        String extractedEmail = jwtUtil.extractUsername(token);
        Long extractedUserNo = jwtUtil.extractUserNo(token);
        String extractedUserRole = jwtUtil.extractUserRole(token);

        assertEquals(user.getEmail(), extractedEmail);
        assertEquals(user.getUserNo(), extractedUserNo);
        assertEquals(user.getUserType().name(), extractedUserRole);
    }

    @Test
    void testTokenPayloadStructure() {
        // Given
        String email = "test@example.com";
        Long userNo = 789L;
        String userRole = "INFLUENCER";

        // When
        String token = jwtUtil.generateAccessToken(email, userNo, userRole);
        Claims claims = jwtUtil.parseAccessClaims(token);

        // Then
        assertEquals(email, claims.getSubject());
        assertEquals("access", claims.get("typ"));
        assertEquals(userNo, claims.get("userNo"));
        assertEquals(userRole, claims.get("userRole"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }
}
