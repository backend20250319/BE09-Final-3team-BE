package site.petful.snsservice.instagram.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        // 인스타그램 Graph API의 기본 URL을 설정합니다.
        return WebClient.builder()
            .baseUrl("https://graph.facebook.com/v23.0")
            .build();
    }
}