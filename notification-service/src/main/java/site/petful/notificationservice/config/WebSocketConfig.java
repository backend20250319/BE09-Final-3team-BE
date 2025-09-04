package site.petful.notificationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@EnableWebSocketMessageBroker
@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // 운영 시 도메인으로 제한
        // SockJS 쓰고 싶으면 .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // RabbitMQ STOMP 브로커로 릴레이
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(System.getenv().getOrDefault("RABBIT_HOST", "localhost"))
                .setRelayPort(15672)
                .setClientLogin(System.getenv().getOrDefault("RABBIT_STOMP_USER", "petful-noti"))
                .setClientPasscode(System.getenv().getOrDefault("RABBIT_STOMP_PASS", "noti1234"));
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user"); // /user/queue/...
    }
}
