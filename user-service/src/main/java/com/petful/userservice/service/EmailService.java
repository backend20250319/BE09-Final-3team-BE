package com.petful.userservice.service;

import com.petful.userservice.dto.EmailVerificationConfirmRequest;
import com.petful.userservice.dto.EmailVerificationRequest;

public interface EmailService {
    void sendVerificationEmail(EmailVerificationRequest request);
    boolean verifyEmailCode(EmailVerificationConfirmRequest request);
}
