package site.petful.gatewayservice.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;
import site.petful.gatewayservice.util.JwtUtil;

import java.util.List;

@Slf4j
@Component("Authentication") // yml의 name: Authentication 과 매칭되게 명시
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public AuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Data
    public static class Config {
        /** 토큰 없거나 무효일 때 401로 막을지 여부 */
        private boolean required = true;

        /** 토큰 없이 통과시킬 경로(게이트웨이에서 보이는 경로 기준) */
        private List<String> whitelist = List.of(
                "/actuator/**",
                "/__ping",
                "/__fallback/**",
                "/api/v1/user-service/auth/**"
        );
    }

    @Override
    public GatewayFilter apply(Config cfg) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            // 1) CORS 프리플라이트는 무조건 통과
            if (request.getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            // 2) 화이트리스트 경로는 통과
            if (isWhitelisted(cfg.getWhitelist(), path)) {
                return chain.filter(exchange);
            }

            // 3) Authorization 헤더 검사
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                return unauthorized(exchange, cfg);
            }

            String token = authHeader.substring(7); // "Bearer " 제거

            // 4) 토큰 유효성/만료 검사
            if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
                log.warn("Invalid or expired JWT token");
                return unauthorized(exchange, cfg);
            }

            // 5) 사용자 ID 추출 및 헤더에 추가 (다운스트림으로 전달)
            String userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                log.warn("Could not extract user ID from token");
                return unauthorized(exchange, cfg);
            }

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-ID", userId)
                    .build();

            log.info("Authentication successful for user: {}", userId);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private boolean isWhitelisted(List<String> patterns, String path) {
        for (String p : patterns) {
            if (p.endsWith("/**")) {
                if (matcher.match(p, path)) return true;
            } else {
                if (path.startsWith(p)) return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorized(org.springframework.web.server.ServerWebExchange exchange, Config cfg) {
        if (!cfg.isRequired()) return exchange.getResponse().setComplete(); // 그냥 통과시킬 수도 있게
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
