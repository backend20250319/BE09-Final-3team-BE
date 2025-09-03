package site.petful.advertiserservice.login.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import site.petful.advertiserservice.repository.AdvertiserRepository;
import site.petful.advertiserservice.login.dto.PasswordResetVerifyResponse;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final AdvertiserRepository advertiserRepository;

    @Value("${mail.from:no-reply@petful.app}")
    private String fromAddress;

    @Value("${auth.password-reset.code-ttl-seconds:300}") // 5분
    private long codeTtlSeconds;

    @Value("${auth.password-reset.max-attempts:5}")       // 검증 시도 5회
    private int maxVerifyAttempts;

    @Value("${auth.password-reset.resend-interval-seconds:60}") // 재전송 최소 간격 60초
    private long resendIntervalSeconds;

    private static final String PASSWORD_RESET_CODE_PREFIX = "advertiser_password_reset_code:";
    private static final String PASSWORD_RESET_ATTEMPTS_PREFIX = "advertiser_password_reset_attempts:";
    private static final String PASSWORD_RESET_LAST_SENT_PREFIX = "advertiser_password_reset_last_sent:";

    public void sendPasswordResetCode(String email) {
        final String normalizedEmail = normalize(email);

        // 이메일 존재 여부 확인
        if (!advertiserRepository.existsByUserId(normalizedEmail)) {
            throw new IllegalStateException("존재하지 않는 이메일입니다.");
        }

        // 재전송 간격 확인
        String lastSentKey = PASSWORD_RESET_LAST_SENT_PREFIX + normalizedEmail;
        String lastSentTime = redisTemplate.opsForValue().get(lastSentKey);
        if (lastSentTime != null) {
            long timeSinceLastSent = System.currentTimeMillis() - Long.parseLong(lastSentTime);
            if (timeSinceLastSent < (resendIntervalSeconds * 1000)) {
                long remainingSeconds = (resendIntervalSeconds * 1000 - timeSinceLastSent) / 1000;
                throw new IllegalStateException(String.format("재전송은 %d초 후에 가능합니다.", remainingSeconds));
            }
        }

        String code = generate6DigitCode();
        String codeKey = PASSWORD_RESET_CODE_PREFIX + normalizedEmail;
        String attemptsKey = PASSWORD_RESET_ATTEMPTS_PREFIX + normalizedEmail;
        
        // Redis에 인증 코드 저장 (TTL 설정)
        redisTemplate.opsForValue().set(codeKey, code, Duration.ofSeconds(codeTtlSeconds));
        redisTemplate.opsForValue().set(attemptsKey, "0", Duration.ofSeconds(codeTtlSeconds));
        redisTemplate.opsForValue().set(lastSentKey, String.valueOf(System.currentTimeMillis()), Duration.ofSeconds(codeTtlSeconds));

        log.info("비밀번호 재설정 인증 코드 저장 완료: {}", normalizedEmail);

        // HTML 메일 전송
        String subject = "Petful 광고주 비밀번호 재설정 인증 코드";
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;font-size:14px">
                  <p>안녕하세요, Petful 광고주 서비스입니다.</p>
                  <p>비밀번호 재설정을 위한 <b>인증 코드</b>입니다.</p>
                  <p>아래 <b>인증 코드</b>를 5분 이내에 입력해주세요.</p>
                  <div style="font-size:24px;letter-spacing:4px;margin:16px 0"><b>%s</b></div>
                  <p>만약 본인이 요청하지 않았다면 이 메일을 무시해도 됩니다.</p>
                  <p>보안을 위해 인증 코드는 5분 후 자동으로 만료됩니다.</p>
                </div>
                """.formatted(code);

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(normalizedEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(mime);
            log.info("비밀번호 재설정 이메일 발송 완료: {}", normalizedEmail);
        } catch (MailException | jakarta.mail.MessagingException e) {
            log.error("비밀번호 재설정 메일 전송 실패: {}", e.getMessage());
            throw new IllegalStateException("메일 전송에 실패했습니다.");
        }
    }

    public boolean verifyPasswordResetCode(String email, String code) {
        final String normalizedEmail = normalize(email);
        final String userInput = code;

        // 0) 입력값 검증
        if (normalizedEmail == null || normalizedEmail.isBlank() || userInput == null || userInput.isBlank()) {
            log.warn("이메일/코드가 비어있음: email={}, code={}", normalizedEmail, userInput);
            return false;
        }

        final String codeKey = PASSWORD_RESET_CODE_PREFIX + normalizedEmail;
        final String attemptsKey = PASSWORD_RESET_ATTEMPTS_PREFIX + normalizedEmail;

        // 1) 저장된 코드 조회
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        if (storedCode == null) {
            log.warn("비밀번호 재설정 인증 코드가 존재하지 않음(만료/미발급): {}", normalizedEmail);
            return false;
        }

        // 2) 시도 횟수 확인
        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
        if (attempts >= maxVerifyAttempts) {
            // 초과 시 정리 후 실패
            redisTemplate.delete(codeKey);
            redisTemplate.delete(attemptsKey);
            log.warn("비밀번호 재설정 인증 시도 횟수 초과: {}", normalizedEmail);
            return false;
        }

        // 3) 코드 일치 → 성공 처리 (여기서는 attempts 증가하지 않음)
        if (storedCode.equals(userInput)) {
            redisTemplate.delete(codeKey);
            redisTemplate.delete(attemptsKey);
            log.info("비밀번호 재설정 인증 성공: {}", normalizedEmail);
            return true;
        }

        // 4) 불일치 → 실패 카운트 +1 (TTL 유지)
        Long after = redisTemplate.opsForValue().increment(attemptsKey);
        // attemptsKey가 사라졌다면 TTL을 다시 부여(드문 케이스 대비)
        if (after != null && after == 1L) {
            redisTemplate.expire(attemptsKey, Duration.ofSeconds(codeTtlSeconds));
        }

        log.warn("비밀번호 재설정 인증 실패: {} (입력 코드: {})", normalizedEmail, userInput);
        return false;
    }

    // 인증 코드 확인만 수행 (비밀번호 변경 없이)
    public PasswordResetVerifyResponse verifyCodeOnly(String email, String code) {
        final String normalizedEmail = normalize(email);
        final String userInput = code;

        // 0) 입력값 검증
        if (normalizedEmail == null || normalizedEmail.isBlank() || userInput == null || userInput.isBlank()) {
            log.warn("이메일/코드가 비어있음: email={}, code={}", normalizedEmail, userInput);
            return PasswordResetVerifyResponse.builder()
                    .email(normalizedEmail)
                    .isValid(false)
                    .message("입력값이 올바르지 않습니다.")
                    .remainingTime(0)
                    .build();
        }

        final String codeKey = PASSWORD_RESET_CODE_PREFIX + normalizedEmail;
        final String attemptsKey = PASSWORD_RESET_ATTEMPTS_PREFIX + normalizedEmail;

        // 1) 저장된 코드 조회
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        if (storedCode == null) {
            log.warn("비밀번호 재설정 인증 코드가 존재하지 않음(만료/미발급): {}", normalizedEmail);
            return PasswordResetVerifyResponse.builder()
                    .email(normalizedEmail)
                    .isValid(false)
                    .message("인증 코드가 만료되었거나 존재하지 않습니다.")
                    .remainingTime(0)
                    .build();
        }

        // 2) 시도 횟수 확인
        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
        if (attempts >= maxVerifyAttempts) {
            log.warn("비밀번호 재설정 인증 시도 횟수 초과: {}", normalizedEmail);
            return PasswordResetVerifyResponse.builder()
                    .email(normalizedEmail)
                    .isValid(false)
                    .message("인증 코드 시도 횟수를 초과했습니다. 다시 요청해주세요.")
                    .remainingTime(0)
                    .build();
        }

        // 3) 코드 일치 확인
        if (storedCode.equals(userInput)) {
            // TTL 확인하여 남은 시간 계산
            Long ttl = redisTemplate.getExpire(codeKey);
            long remainingSeconds = ttl != null ? ttl : 0;
            
            log.info("인증 코드 확인 성공: {} (남은 시간: {}초)", normalizedEmail, remainingSeconds);
            return PasswordResetVerifyResponse.builder()
                    .email(normalizedEmail)
                    .isValid(true)
                    .message("인증 코드가 확인되었습니다.")
                    .remainingTime(remainingSeconds)
                    .build();
        }

        // 4) 불일치 → 실패 카운트 +1 (TTL 유지)
        Long after = redisTemplate.opsForValue().increment(attemptsKey);
        if (after != null && after == 1L) {
            redisTemplate.expire(attemptsKey, Duration.ofSeconds(codeTtlSeconds));
        }

        // 남은 시도 횟수 계산
        int remainingAttempts = maxVerifyAttempts - (attempts + 1);
        
        log.warn("인증 코드 확인 실패: {} (입력 코드: {}, 남은 시도: {})", normalizedEmail, userInput, remainingAttempts);
        return PasswordResetVerifyResponse.builder()
                .email(normalizedEmail)
                .isValid(false)
                .message(String.format("인증 코드가 올바르지 않습니다. 남은 시도 횟수: %d회", remainingAttempts))
                .remainingTime(0)
                .build();
    }

    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String generate6DigitCode() {
        return String.format("%06d",
                ThreadLocalRandom.current().nextInt(100000, 1_000_000));
    }
}
