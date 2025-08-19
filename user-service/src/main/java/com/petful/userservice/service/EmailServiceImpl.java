package com.petful.userservice.service;

import com.petful.userservice.dto.EmailVerificationConfirmRequest;
import com.petful.userservice.dto.EmailVerificationRequest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    // 환경 변수/설정 파일로 분리 권장
    @Value("${mail.from:no-reply@petful.app}")
    private String fromAddress;

    @Value("${auth.email.code-ttl-seconds:300}") // 5분
    private long codeTtlSeconds;

    @Value("${auth.email.max-attempts:5}")       // 검증 시도 5회
    private int maxVerifyAttempts;

    @Value("${auth.email.resend-interval-seconds:60}") // 재전송 최소 간격 60초
    private long resendIntervalSeconds;

    private static final String VERIFICATION_CODE_PREFIX = "email_verification:";
    private static final String VERIFICATION_ATTEMPTS_PREFIX = "email_attempts:";
    private static final String LAST_SENT_PREFIX = "email_last_sent:";

    @Override
    public void sendVerificationEmail(EmailVerificationRequest request) {
        final String email = normalize(request.getEmail());

        // 재전송 간격 제한
        String lastSentKey = LAST_SENT_PREFIX + email;
        String lastSentTime = redisTemplate.opsForValue().get(lastSentKey);
        if (lastSentTime != null) {
            LocalDateTime lastSent = LocalDateTime.parse(lastSentTime);
            if (Duration.between(lastSent, LocalDateTime.now()).getSeconds() < resendIntervalSeconds) {
                throw new IllegalStateException("재전송 제한: 잠시 후 다시 시도하라");
            }
        }

        String code = generate6DigitCode();
        String codeKey = VERIFICATION_CODE_PREFIX + email;
        String attemptsKey = VERIFICATION_ATTEMPTS_PREFIX + email;
        
        // Redis에 인증 코드 저장 (TTL 설정)
        redisTemplate.opsForValue().set(codeKey, code, Duration.ofSeconds(codeTtlSeconds));
        redisTemplate.opsForValue().set(attemptsKey, "0", Duration.ofSeconds(codeTtlSeconds));
        redisTemplate.opsForValue().set(lastSentKey, LocalDateTime.now().toString(), Duration.ofSeconds(codeTtlSeconds));

        log.info("인증 코드 저장 완료: {}", email);

        // HTML 메일 전송
        String subject = "Petful 이메일 인증 코드";
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;font-size:14px">
                  <p>안녕하세요, Petful 입니다.</p>
                  <p>아래 <b>인증 코드</b>를 5분 이내에 입력해 이메일을 인증해주세요.</p>
                  <div style="font-size:24px;letter-spacing:4px;margin:16px 0"><b>%s</b></div>
                  <p>만약 본인이 요청하지 않았다면 이 메일을 무시해도 됩니다.</p>
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
            log.info("인증 이메일 발송 완료: {}", email);
        } catch (MailException | jakarta.mail.MessagingException e) {
            log.error("메일 전송 실패: {}", e.getMessage());
            throw new IllegalStateException("메일 전송에 실패했다");
        }
    }

    @Override
    public boolean verifyEmailCode(EmailVerificationConfirmRequest request) {
        final String email = normalize(request.getEmail());
        String codeKey = VERIFICATION_CODE_PREFIX + email;
        String attemptsKey = VERIFICATION_ATTEMPTS_PREFIX + email;
        
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        
        if (storedCode == null) {
            log.warn("인증 코드가 존재하지 않음: {}", email);
            return false; // 코드가 없음 (만료되었거나 존재하지 않음)
        }
        
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
        if (attempts >= maxVerifyAttempts) {
            // 시도 횟수 초과 시 Redis에서 삭제
            redisTemplate.delete(codeKey);
            redisTemplate.delete(attemptsKey);
            log.warn("인증 시도 횟수 초과: {}", email);
            return false;
        }
        
        // 시도 횟수 증가
        redisTemplate.opsForValue().increment(attemptsKey);
        
        if (storedCode.equals(request.getVerificationCode())) {
            // 인증 성공 시 Redis에서 모든 관련 데이터 삭제
            redisTemplate.delete(codeKey);
            redisTemplate.delete(attemptsKey);
            redisTemplate.delete(LAST_SENT_PREFIX + email);
            log.info("이메일 인증 성공: {}", email);
            return true;
        }
        
        log.warn("이메일 인증 실패: {} (입력 코드: {})", email, request.getVerificationCode());
        return false;
    }

    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String generate6DigitCode() {
        return String.format("%06d",
                ThreadLocalRandom.current().nextInt(100000, 1_000_000));
    }


}
