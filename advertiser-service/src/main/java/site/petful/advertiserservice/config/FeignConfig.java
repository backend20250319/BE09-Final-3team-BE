package site.petful.advertiserservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import site.petful.advertiserservice.security.JwtTokenProvider;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FeignConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public RequestInterceptor advertiserAuthInterceptor() {
        return template -> {
            // 현재 요청에서 사용자 정보를 가져와서 전달
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // Authorization 헤더에서 JWT 토큰 추출
                String authorizationHeader = request.getHeader("Authorization");
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    String token = authorizationHeader.substring(7);

                    // JWT 토큰 유효성 검사
                    if (jwtTokenProvider.validateAccessToken(token)) {
                        // JWT 토큰에서 직접 사용자 정보 추출
                        Long advertiserNo = jwtTokenProvider.getAdvertiserNoFromAccessToken(token);
                        String userType = jwtTokenProvider.getUserTypeFromAccessToken(token);

                        template.header("Authorization", authorizationHeader);

                        if (advertiserNo != null) {
                            template.header("X-User-No", advertiserNo.toString());
                        }
                        if (userType != null) {
                            template.header("X-User-Type", userType);
                        }
                    }
                }
            }
        };
    }
}