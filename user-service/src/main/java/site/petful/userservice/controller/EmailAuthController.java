package site.petful.userservice.controller;

import site.petful.userservice.dto.EmailVerificationConfirmRequest;
import site.petful.userservice.dto.EmailVerificationRequest;
import site.petful.userservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class EmailAuthController {
    private final EmailService emailService;

    @PostMapping("/email/send")
    public ResponseEntity<?> send(@RequestBody EmailVerificationRequest req) {
        try {
            emailService.sendVerificationEmail(req);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            
            if (e.getMessage().contains("이미 가입된 이메일")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            } else {
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
    }

    @PostMapping("/email/verify")
    public ResponseEntity<Boolean> verify(@RequestBody EmailVerificationConfirmRequest req) {
        boolean ok = emailService.verifyEmailCode(req);
        return ResponseEntity.ok(ok);

    }
}


