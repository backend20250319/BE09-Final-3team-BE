package site.petful.userservice.common;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

class UserHeaderUtilTest {

    @Test
    void getCurrentUserNo_WithValidHeader_ShouldReturnUserNo() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-No", "123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // When
        Long userNo = UserHeaderUtil.getCurrentUserNo();

        // Then
        assertEquals(123L, userNo);
    }

    @Test
    void getCurrentUserNo_WithInvalidHeader_ShouldReturnNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-No", "invalid");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // When
        Long userNo = UserHeaderUtil.getCurrentUserNo();

        // Then
        assertNull(userNo);
    }

    @Test
    void getCurrentUserNo_WithNoHeader_ShouldReturnNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // When
        Long userNo = UserHeaderUtil.getCurrentUserNo();

        // Then
        assertNull(userNo);
    }

    @Test
    void getCurrentUserType_WithValidHeader_ShouldReturnUserType() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Type", "User");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // When
        String userType = UserHeaderUtil.getCurrentUserType();

        // Then
        assertEquals("User", userType);
    }

    @Test
    void getCurrentUserType_WithNoHeader_ShouldReturnNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // When
        String userType = UserHeaderUtil.getCurrentUserType();

        // Then
        assertNull(userType);
    }

    @Test
    void getCurrentUserNoOrThrow_WithValidHeader_ShouldReturnUserNo() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-No", "456");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // When & Then
        assertDoesNotThrow(() -> {
            Long userNo = UserHeaderUtil.getCurrentUserNoOrThrow();
            assertEquals(456L, userNo);
        });
    }

    @Test
    void getCurrentUserNoOrThrow_WithNoHeader_ShouldThrowException() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            UserHeaderUtil.getCurrentUserNoOrThrow();
        });
    }

    @Test
    void getCurrentUserTypeOrThrow_WithValidHeader_ShouldReturnUserType() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Type", "Admin");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // When & Then
        assertDoesNotThrow(() -> {
            String userType = UserHeaderUtil.getCurrentUserTypeOrThrow();
            assertEquals("Admin", userType);
        });
    }

    @Test
    void getCurrentUserTypeOrThrow_WithNoHeader_ShouldThrowException() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            UserHeaderUtil.getCurrentUserTypeOrThrow();
        });
    }
}
