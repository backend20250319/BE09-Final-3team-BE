package site.petful.campaignservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor userAuthInterceptor() {
        return template -> {
            // 현재 요청에서 사용자 정보를 가져와서 전달
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // 사용자 토큰 전달
                String authorizationHeader = request.getHeader("Authorization");
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    template.header("Authorization", authorizationHeader);
                }
                
                // Gateway에서 전달한 사용자 정보 헤더들도 전달
                String userNo = request.getHeader("X-User-No");
                String userType = request.getHeader("X-User-Type");
                
                if (userNo != null) {
                    template.header("X-User-No", userNo);
                }
                if (userType != null) {
                    template.header("X-User-Type", userType);
                }
            }
        };
    }
}