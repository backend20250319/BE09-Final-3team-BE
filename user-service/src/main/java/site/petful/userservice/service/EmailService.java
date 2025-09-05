package site.petful.userservice.service;

import site.petful.userservice.dto.EmailVerificationConfirmRequest;
import site.petful.userservice.dto.EmailVerificationRequest;

public interface EmailService {
    void sendVerificationEmail(EmailVerificationRequest request);
    boolean verifyEmailCode(EmailVerificationConfirmRequest request);
    
    // 일반 이메일 발송 메서드
    void sendEmail(String to, String subject, String content);
}
