package site.petful.userservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import site.petful.userservice.security.CustomUserDetailsService;
import site.petful.userservice.security.HeaderAuthenticationFilter;



@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final HeaderAuthenticationFilter headerBasedAuthFilter;

    // 공개 엔드포인트 (인증 불필요)
    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/login",
            "/auth/refresh",
            "/auth/validate-token",
            "/auth/signup",
            "/auth/email/send",
            "/auth/email/verify",
            "/auth/password/reset",
            "/auth/password/verify",
            "/auth/password/change",
            "/auth/profile/simple",        // community-service에서 호출하는 엔드포인트
            "/auth/profile/simple/batch",  // community-service에서 호출하는 배치 엔드포인트
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/validate-token",
            "/api/auth/signup",
            "/api/auth/password/reset",
            "/api/auth/password/verify",
            "/api/auth/password/change",
            "/api/v1/admin/users/logout",  // Admin 로그아웃 엔드포인트 공개
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/actuator/**"
    };
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CORS & CSRF
                .cors(cors -> cors.disable()) // CORS 비활성화 (Gateway에서 처리)
                .csrf(csrf -> csrf.disable())

                // 예외 응답을 JSON으로 일관화
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthEntryPoint())
                        .accessDeniedHandler(restAccessDeniedHandler())
                )

                // 완전 무상태 세션
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인가 규칙
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 프리플라이트 허용
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers("/error").permitAll()  // (선택) 기본 에러 핸들러 공개
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")  // 임시로 ADMIN 권한 체크 비활성화
                        .anyRequest().authenticated()
                )

                // 커스텀 인증 프로바이더 + JWT 필터 + 헤더 기반 인증 필터
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(headerBasedAuthFilter, UsernamePasswordAuthenticationFilter.class);
//                .addFilterBefore(jwtAuthFilter, HeaderBasedAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // --- JSON 401/403 핸들러 ---
    @Bean
    public AuthenticationEntryPoint restAuthEntryPoint() {
        return (request, response, ex) -> {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Unauthorized\"}");
        };
    }

    @Bean
    public AccessDeniedHandler restAccessDeniedHandler() {
        return (request, response, ex) -> {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Forbidden\"}");
        };
    }
}
