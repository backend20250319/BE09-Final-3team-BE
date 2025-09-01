package site.petful.snsservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import site.petful.snsservice.jwt.HeaderAuthenticationFilter;
import site.petful.snsservice.jwt.RestAccessDeniedHandler;
import site.petful.snsservice.jwt.RestAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .formLogin(FormLoginConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)

            .sessionManagement(session
                -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(
                exception ->
                    exception.accessDeniedHandler(restAccessDeniedHandler)
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
            )

            .authorizeHttpRequests(auth ->
                auth
                    .requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs/**",
                        "/swagger-resources/**").permitAll()

                    .requestMatchers("/batch/instagram/**", "/clova/**")
                    .hasAuthority("Admin")

                    .requestMatchers("/instagram/auth/**", "/instagram/profiles/**",
                        "/instagram/comments/**",
                        "/instagram/insights/**", "/instagram/media/**")
                    .hasAnyAuthority("User", "Admin")
                    .anyRequest().authenticated()
            )
            .addFilterBefore(headerAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class)
        ;

        return http.build();
    }

    @Bean
    public HeaderAuthenticationFilter headerAuthenticationFilter() {
        return new HeaderAuthenticationFilter();
    }

}