package site.petful.communityservice.security;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Configuration
public class FeignAuthConfig {

    @Bean
    public RequestInterceptor authForwardingInterceptor() {
        return template -> {
            try {
                var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs == null) {
                    log.warn("RequestContextHolder에서 RequestAttributes를 가져올 수 없습니다. FeignClient 호출 시 컨텍스트가 없을 수 있습니다.");
                    return;
                }
                
                var req = attrs.getRequest();

                // 1) 사용자 토큰 그대로 전달
                String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
                if (StringUtils.hasText(auth)) {
                    template.header(HttpHeaders.AUTHORIZATION, auth);
                    log.info("FeignClient에 Authorization 헤더 전달: {}...", auth.substring(0, Math.min(20, auth.length())));
                } else {
                    log.warn("Authorization 헤더가 없습니다. Request URI: {}", req.getRequestURI());
                }

                // 2) (선택) 게이트웨이에서 추출한 사용자 번호도 함께 전달
                String userNo = req.getHeader("X-User-No");
                if (StringUtils.hasText(userNo)) {
                    template.header("X-User-No", userNo);
                    log.info("FeignClient에 X-User-No 헤더 전달: {}", userNo);
                } else {
                    log.warn("X-User-No 헤더가 없습니다. Request URI: {}", req.getRequestURI());
                }
            } catch (Exception e) {
                log.error("FeignClient 인증 헤더 전달 중 오류 발생", e);
            }
        };
    }
}
