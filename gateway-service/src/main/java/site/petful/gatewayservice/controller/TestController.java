package site.petful.gatewayservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import site.petful.gatewayservice.util.JwtTokenGenerator;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final JwtTokenGenerator jwtTokenGenerator;

    @GetMapping("/auth")
    public Mono<ResponseEntity<String>> testAuth(@RequestHeader("X-User-ID") String userId) {
        log.info("Test endpoint called with user ID: {}", userId);
        return Mono.just(ResponseEntity.ok("Authentication successful! User ID: " + userId));
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<String>> health() {
        return Mono.just(ResponseEntity.ok("Gateway Service is running!"));
    }

    @PostMapping("/generate-token")
    public Mono<ResponseEntity<Map<String, String>>> generateToken(@RequestParam String userId) {
        String token = jwtTokenGenerator.generateToken(userId);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", userId);
        log.info("Generated token for user: {}", userId);
        return Mono.just(ResponseEntity.ok(response));
    }
}
