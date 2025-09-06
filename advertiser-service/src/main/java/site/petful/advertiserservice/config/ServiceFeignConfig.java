package site.petful.advertiserservice.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import site.petful.advertiserservice.security.JwtTokenProvider;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ServiceFeignConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public RequestInterceptor serviceAuthInterceptor() {
        return template -> {
            // 서비스 간 통신용 토큰 생성
            String serviceToken = jwtTokenProvider.generateServiceToken();
            
            // Authorization 헤더에 서비스 토큰 추가
            template.header("Authorization", "Bearer " + serviceToken);
            template.header("X-User-Type", "SERVICE");
            
            log.debug("Service token generated for inter-service communication");
        };
    }
}
