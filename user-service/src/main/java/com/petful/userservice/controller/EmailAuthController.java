package com.petful.userservice.controller;

import com.petful.userservice.dto.EmailVerificationConfirmRequest;
import com.petful.userservice.dto.EmailVerificationRequest;
import com.petful.userservice.service.EmailService;
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


