package site.petful.userservice.controller;

import site.petful.userservice.dto.EmailVerificationConfirmRequest;
import site.petful.userservice.dto.EmailVerificationRequest;
import site.petful.userservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class EmailAuthController {
    private final EmailService emailService;

    @PostMapping("/email/send")
    public ResponseEntity<Void> send(@RequestBody EmailVerificationRequest req) {
        emailService.sendVerificationEmail(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/verify")
    public ResponseEntity<Boolean> verify(@RequestBody EmailVerificationConfirmRequest req) {
        boolean ok = emailService.verifyEmailCode(req);
        return ResponseEntity.ok(ok);

    }
}


