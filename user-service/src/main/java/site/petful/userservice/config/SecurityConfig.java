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
import site.petful.userservice.security.JwtAuthenticationFilter;



@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    // 게이트웨이 리라이트 유무 모두 커버(둘 다 열어둠)
    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/**",
            "/api/auth/**",
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
                        .requestMatchers("/error").permitAll()                  // (선택) 기본 에러 핸들러 공개

                        .anyRequest().authenticated()
                )

                // 커스텀 인증 프로바이더 + JWT 필터
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

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
