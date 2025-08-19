package site.petful.userservice.service;

import site.petful.userservice.dto.EmailVerificationConfirmRequest;
import site.petful.userservice.dto.EmailVerificationRequest;

public interface EmailService {
    void sendVerificationEmail(EmailVerificationRequest request);
    boolean verifyEmailCode(EmailVerificationConfirmRequest request);
}
