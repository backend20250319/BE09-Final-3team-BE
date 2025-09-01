package site.petful.userservice.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class UserHeaderUtil {

    private static final String USER_NO_HEADER = "X-User-No";
    private static final String USER_TYPE_HEADER = "X-User-Type";

    /**
     * 현재 요청에서 X-User-No 헤더 값을 추출합니다.
     * @return 사용자 번호, 헤더가 없거나 파싱 실패 시 null
     */
    public static Long getCurrentUserNo() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.warn("RequestContextHolder에서 ServletRequestAttributes를 가져올 수 없습니다.");
                return null;
            }

            HttpServletRequest request = attributes.getRequest();
            String userNoStr = request.getHeader(USER_NO_HEADER);
            
            if (userNoStr == null || userNoStr.trim().isEmpty()) {
                log.debug("X-User-No 헤더가 없습니다.");
                return null;
            }

            return Long.parseLong(userNoStr.trim());
        } catch (NumberFormatException e) {
            log.warn("X-User-No 헤더 값을 Long으로 파싱할 수 없습니다: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("X-User-No 헤더 추출 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 현재 요청에서 X-User-Type 헤더 값을 추출합니다.
     * @return 사용자 타입, 헤더가 없으면 null
     */
    public static String getCurrentUserType() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.warn("RequestContextHolder에서 ServletRequestAttributes를 가져올 수 없습니다.");
                return null;
            }

            HttpServletRequest request = attributes.getRequest();
            String userType = request.getHeader(USER_TYPE_HEADER);
            
            if (userType == null || userType.trim().isEmpty()) {
                log.debug("X-User-Type 헤더가 없습니다.");
                return null;
            }

            return userType.trim();
        } catch (Exception e) {
            log.warn("X-User-Type 헤더 추출 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 현재 요청에서 사용자 번호를 추출하고, 없으면 예외를 발생시킵니다.
     * @return 사용자 번호
     * @throws IllegalStateException 헤더가 없거나 파싱 실패 시
     */
    public static Long getCurrentUserNoOrThrow() {
        Long userNo = getCurrentUserNo();
        if (userNo == null) {
            throw new IllegalStateException("X-User-No 헤더가 없거나 유효하지 않습니다.");
        }
        return userNo;
    }

    /**
     * 현재 요청에서 사용자 타입을 추출하고, 없으면 예외를 발생시킵니다.
     * @return 사용자 타입
     * @throws IllegalStateException 헤더가 없을 때
     */
    public static String getCurrentUserTypeOrThrow() {
        String userType = getCurrentUserType();
        if (userType == null) {
            throw new IllegalStateException("X-User-Type 헤더가 없습니다.");
        }
        return userType;
    }
}
