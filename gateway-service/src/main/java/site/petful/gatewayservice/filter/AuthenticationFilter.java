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
@Component("Authentication")
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

            // 1) 프리플라이트(OPTIONS)는 무조건 통과
            if (request.getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            // 2) 화이트리스트 경로는 통과
            if (isWhitelisted(cfg.getWhitelist(), path)) {
                return chain.filter(exchange);
            }

            // 3) Authorization 헤더 검사
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
                log.debug("Missing or invalid Authorization header for path {}", path);
                return cfg.required ? unauthorized(exchange) : chain.filter(exchange);
            }

            String token = authHeader.substring(7).trim(); // "Bearer " 제거 후 공백 제거

            // 4) 토큰 유효성/만료 검사 (예외 방어)
            try {
                if (!jwtUtil.validateToken(token)) {
                    log.debug("JWT validation failed");
                    return cfg.required ? unauthorized(exchange) : chain.filter(exchange);
                }
                if (jwtUtil.isTokenExpired(token)) { // validateToken이 만료 포함하면 이 줄은 제거 가능
                    log.debug("JWT expired");
                    return cfg.required ? unauthorized(exchange) : chain.filter(exchange);
                }
            } catch (Exception ex) {
                log.debug("JWT parse/validate exception: {}", ex.getMessage());
                return cfg.required ? unauthorized(exchange) : chain.filter(exchange);
            }

            // 5) 사용자 ID 추출 및 헤더에 추가
            String userId = null;
            try {
                userId = jwtUtil.getUserIdFromToken(token);
            } catch (Exception ex) {
                log.debug("Failed to extract userId from token: {}", ex.getMessage());
            }
            if (userId == null || userId.isBlank()) {
                return cfg.required ? unauthorized(exchange) : chain.filter(exchange);
            }

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-ID", userId)
                    .build();

            log.debug("Authentication ok, userId={}", userId);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private boolean isWhitelisted(List<String> patterns, String path) {
        if (patterns == null) return false;
        for (String p : patterns) {
            // AntPathMatcher가 exact 패턴도 지원하므로 일괄 match로 단순화
            if (matcher.match(p, path)) return true;
        }
        return false;
    }

    private Mono<Void> unauthorized(org.springframework.web.server.ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        // (선택) 캐시 금지 헤더를 넣고 싶으면 아래 추가
        // exchange.getResponse().getHeaders().add("Cache-Control", "no-store");
        return exchange.getResponse().setComplete();
    }
}
