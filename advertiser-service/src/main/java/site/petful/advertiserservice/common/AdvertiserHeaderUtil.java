package site.petful.advertiserservice.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class AdvertiserHeaderUtil {
    
    private static final String USER_NO_HEADER = "X-User-No";
    private static final String USER_TYPE_HEADER = "X-User-Type";
    
    /**
     * 현재 요청의 X-User-No 헤더에서 광고주 번호를 추출합니다.
     * @return 광고주 번호, 헤더가 없거나 파싱 실패 시 null
     */
    public static Long getCurrentAdvertiserNo() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.warn("RequestContextHolder에서 ServletRequestAttributes를 가져올 수 없습니다.");
                return null;
            }
            
            HttpServletRequest request = attributes.getRequest();
            String advertiserNoStr = request.getHeader(USER_NO_HEADER);
            
            if (advertiserNoStr == null || advertiserNoStr.trim().isEmpty()) {
                log.debug("X-User-No 헤더가 없습니다.");
                return null;
            }
            
            return Long.parseLong(advertiserNoStr.trim());
        } catch (NumberFormatException e) {
            log.warn("X-User-No 헤더 값을 Long으로 파싱할 수 없습니다: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("X-User-No 헤더 추출 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 현재 요청의 X-User-Type 헤더에서 사용자 타입을 추출합니다.
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
            return request.getHeader(USER_TYPE_HEADER);
        } catch (Exception e) {
            log.warn("X-User-Type 헤더 추출 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 현재 요청의 X-User-No 헤더에서 광고주 번호를 추출합니다.
     * 헤더가 없거나 파싱 실패 시 예외를 발생시킵니다.
     * @return 광고주 번호
     * @throws IllegalStateException 헤더가 없거나 파싱 실패 시
     */
    public static Long getCurrentAdvertiserNoOrThrow() {
        Long advertiserNo = getCurrentAdvertiserNo();
        if (advertiserNo == null) {
            throw new IllegalStateException("X-User-No 헤더에서 광고주 번호를 찾을 수 없습니다.");
        }
        return advertiserNo;
    }
    
    /**
     * 현재 요청의 X-User-Type 헤더에서 사용자 타입을 추출합니다.
     * 헤더가 없으면 예외를 발생시킵니다.
     * @return 사용자 타입
     * @throws IllegalStateException 헤더가 없을 시
     */
    public static String getCurrentUserTypeOrThrow() {
        String userType = getCurrentUserType();
        if (userType == null || userType.trim().isEmpty()) {
            throw new IllegalStateException("X-User-Type 헤더에서 사용자 타입을 찾을 수 없습니다.");
        }
        return userType;
    }
}