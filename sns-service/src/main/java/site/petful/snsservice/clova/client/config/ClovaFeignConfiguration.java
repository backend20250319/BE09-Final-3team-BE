package site.petful.snsservice.clova.client.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClovaFeignConfiguration {

    // application.yml (또는 .properties) 파일에서 API 키를 주입받습니다.
    @Value("${clova.api.key}")
    private String clovaApiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // 모든 요청에 아래 헤더들을 추가합니다.
            requestTemplate.header("Authorization", "Bearer " + clovaApiKey);
            requestTemplate.header("Content-Type", "application/json");
        };
    }
}