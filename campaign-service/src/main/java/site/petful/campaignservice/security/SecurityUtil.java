package site.petful.campaignservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public Long getCurrentUserNo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String principalString = authentication.getPrincipal().toString();
            try {
                return Long.parseLong(principalString);
            } catch (NumberFormatException e) {
                throw new RuntimeException("사용자 번호가 올바른 숫자가 아닙니다: " + principalString);
            }
        }
        throw new RuntimeException("인증된 사용자 정보를 찾을 수 없습니다.");
    }

}
