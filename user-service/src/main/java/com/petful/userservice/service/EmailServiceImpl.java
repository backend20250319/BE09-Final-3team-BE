package com.petful.userservice.service;

import com.petful.userservice.dto.EmailVerificationConfirmRequest;
import com.petful.userservice.dto.EmailVerificationRequest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    // 환경 변수/설정 파일로 분리 권장
    @Value("${mail.from:no-reply@petful.app}")
    private String fromAddress;

    @Value("${auth.email.code-ttl-seconds:300}") // 5분
    private long codeTtlSeconds;

    @Value("${auth.email.max-attempts:5}")       // 검증 시도 5회
    private int maxVerifyAttempts;

    @Value("${auth.email.resend-interval-seconds:60}") // 재전송 최소 간격 60초
    private long resendIntervalSeconds;

    private final Map<String, VerificationEntry> store = new ConcurrentHashMap<>();

    @Override
    public void sendVerificationEmail(EmailVerificationRequest request) {
        final String email = normalize(request.getEmail());

        // 재전송 간격 제한
        var existing = store.get(email);
        if (existing != null && Duration.between(existing.getLastSentAt(), LocalDateTime.now())
                .getSeconds() < resendIntervalSeconds) {
            throw new IllegalStateException("재전송 제한: 잠시 후 다시 시도하라");
        }

        String code = generate6DigitCode();
        var entry = VerificationEntry.builder()
                .code(code)
                .expiresAt(LocalDateTime.now().plusSeconds(codeTtlSeconds))
                .attempts(0)
                .lastSentAt(LocalDateTime.now())
                .build();
        store.put(email, entry);

        // HTML 메일 전송
        String subject = "Petful 이메일 인증 코드";
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;font-size:14px">
                  <p>안녕하세요, Petful 입니다.</p>
                  <p>아래 <b>인증 코드</b>를 5분 이내에 입력해 이메일을 인증하라.</p>
                  <div style="font-size:24px;letter-spacing:4px;margin:16px 0"><b>%s</b></div>
                  <p>만약 본인이 요청하지 않았다면 이 메일을 무시해도 된다.</p>
                </div>
                """.formatted(code);

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(mime);
        } catch (MailException | jakarta.mail.MessagingException e) {
            log.error("메일 전송 실패: {}", e.getMessage());
            throw new IllegalStateException("메일 전송에 실패했다");
        }
    }

    @Override
    public boolean verifyEmailCode(EmailVerificationConfirmRequest request) {
        final String email = normalize(request.getEmail());
        var entry = store.get(email);

        if (entry == null) return false;
        if (LocalDateTime.now().isAfter(entry.getExpiresAt())) {
            store.remove(email);
            return false; // 만료
        }
        if (entry.getAttempts() >= maxVerifyAttempts) {
            store.remove(email);
            return false; // 시도 초과
        }

        entry.setAttempts(entry.getAttempts() + 1);
        if (entry.getCode().equals(request.getVerificationCode())) {
            store.remove(email); // 일회성 사용
            // TODO: 여기에서 UserRepository 등을 사용해 isVerified=true 업데이트
            // userRepository.markEmailVerified(email);
            return true;
        }
        return false;
    }

    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String generate6DigitCode() {
        return String.format("%06d",
                ThreadLocalRandom.current().nextInt(100000, 1_000_000));
    }

    @Getter @Setter @Builder
    private static class VerificationEntry {
        private String code;
        private LocalDateTime expiresAt;
        private int attempts;
        private LocalDateTime lastSentAt;
    }
}
