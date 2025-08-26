package site.petful.gatewayservice.filter;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import site.petful.gatewayservice.util.JwtUtil;

@Slf4j
@Component

public class AuthenticationFilter extends
    AbstractGatewayFilterFactory<AuthenticationFilter.Config> {


    @Override
    public String name() {
        return "Authentication";
    }

    private static final String HDR_USER_NO = "X-User-No";
    private static final String HDR_USER_TYPE = "X-User-Type";

    // 기본 화이트리스트 - 인증 없이 접근 가능한 경로들
    private static final List<String> DEFAULT_WHITELIST = List.of(
        "/api/v1/user-service/auth/**",
        "/api/v1/user-service/health",
        "/api/v1/advertiser-service/advertiser/**",
        "/api/v1/advertiser-service/health",
        "/api/v1/advertiser-service/advertiser/email/**",
        "/actuator/**"
    );

    private final JwtUtil jwtUtil;

    public AuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config cfg) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            // OPTIONS 요청은 통과
            if (request.getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            // 화이트리스트 경로는 통과
            if (isWhitelisted(cfg.getWhitelist(), path)) {
                return chain.filter(exchange);
            }

            // Authorization 헤더 검사
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("Missing or invalid Authorization header for path {}", path);
                return cfg.required ? unauthorized(exchange) : chain.filter(exchange);
            }

            String token = authHeader.substring(7).trim();

            // 토큰 유효성 검사
            if (!jwtUtil.validateToken(token)) {
                log.debug("JWT validation failed for path {}", path);
                return cfg.required ? unauthorized(exchange) : chain.filter(exchange);
            }

            // userNo, userType 추출
            String userNo = jwtUtil.getUserNoFromToken(token);
            String userType = jwtUtil.getUserTypeFromToken(token);

            if (userNo == null || userType == null) {
                log.warn("Missing userNo or userType in token for path: {}", path);
                return cfg.required ? unauthorized(exchange) : chain.filter(exchange);
            }

            // 헤더에 추가
            ServerHttpRequest modifiedRequest = request.mutate()
                .header(HDR_USER_NO, userNo)
                .header(HDR_USER_TYPE, userType)
                .build();

            log.debug("Authentication successful - userNo: {}, userType: {}, path: {}", userNo,
                userType, path);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private boolean isWhitelisted(List<String> whitelist, String path) {
        // 기본 화이트리스트 확인
        if (DEFAULT_WHITELIST.stream().anyMatch(pattern ->
            path.matches(pattern.replace("**", ".*")))) {
            return true;
        }

        // 설정된 화이트리스트 확인
        if (whitelist == null) {
            return false;
        }
        return whitelist.stream().anyMatch(pattern ->
            path.matches(pattern.replace("**", ".*")));
    }

    private reactor.core.publisher.Mono<Void> unauthorized(
        org.springframework.web.server.ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    public static class Config {

        private boolean required = true;
        private List<String> whitelist;

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public List<String> getWhitelist() {
            return whitelist;
        }

        public void setWhitelist(List<String> whitelist) {
            this.whitelist = whitelist;
        }
    }
}
