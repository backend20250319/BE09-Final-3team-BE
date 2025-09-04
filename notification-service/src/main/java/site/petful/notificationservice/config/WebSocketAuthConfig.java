package site.petful.notificationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.token.TokenService;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import site.petful.notificationservice.security.TokenProvider;

import java.security.Principal;

@Configuration
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {

    private TokenProvider tokenProvider;

    @Override
    public void configureClientInboundChannel(ChannelRegistration reg) {
        reg.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> msg, MessageChannel ch) {
                StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(msg, StompHeaderAccessor.class);
                if (acc != null && StompCommand.CONNECT.equals(acc.getCommand())) {
                    // 대소문자 모두 대비
                    String auth = firstNonBlank(
                            acc.getFirstNativeHeader("Authorization"),
                            acc.getFirstNativeHeader("authorization")
                    );

                    // (옵션) X-User-No를 직접 보냈다면 그걸로도 허용
                    String userNoHdr = firstNonBlank(
                            acc.getFirstNativeHeader("X-User-No"),
                            acc.getFirstNativeHeader("x-user-no")
                    );

                    Long userNo = (userNoHdr != null) ? parseLongOrNull(userNoHdr)
                            : tokenProvider.resolveUserNoFromAuthHeader(auth);

                    if (userNo == null) {
                        return null; // 인증 실패 → 연결 거부 (정책에 맞게 처리)
                    }

                    Principal p = () -> String.valueOf(userNo);
                    acc.setUser(p);
                    acc.setLeaveMutable(true); // 변경 유지
                }
                return msg;
            }
        });
    }
    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : ((b != null && !b.isBlank()) ? b : null);
    }
    private static Long parseLongOrNull(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return null; }
    }
}
