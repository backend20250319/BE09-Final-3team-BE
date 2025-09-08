package site.petful.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 웹푸시 구독 정보를 저장하는 엔티티
 * 
 * 브라우저에서 생성된 구독 정보를 서버에 저장하여
 * 나중에 해당 사용자에게 웹푸시를 발송할 때 사용합니다.
 */
@Entity
@Table(name = "web_push_subscriptions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class WebPushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @Column(name = "user_no", nullable = false)
    private Long userId;

    /**
     * 브라우저에서 생성된 구독 엔드포인트
     * 예: https://fcm.googleapis.com/fcm/send/...
     */
    @Column(name = "endpoint", nullable = false, length = 512)
    private String endpoint;

    /**
     * P-256 공개키 (Base64 인코딩)
     * 클라이언트에서 생성한 공개키
     */
    @Column(name = "p256dh_key", nullable = false, length = 256)
    private String p256dhKey;

    /**
     * 인증 키 (Base64 인코딩)
     * 클라이언트에서 생성한 인증 키
     */
    @Column(name = "auth_key", nullable = false, length = 256)
    private String authKey;

    /**
     * 사용자 에이전트 정보
     * 어떤 브라우저/디바이스에서 구독했는지 추적
     */
    @Column(name = "user_agent", length = 512)
    private String userAgent;

    /**
     * 구독 활성화 상태
     * false면 해당 구독으로 푸시를 보내지 않음
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * 마지막 푸시 발송 시간
     * 구독 상태 확인용
     */
    @Column(name = "last_push_at")
    private LocalDateTime lastPushAt;

    /**
     * 구독 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 구독 정보 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 구독 비활성화
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    /**
     * 구독 활성화
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    /**
     * 푸시 발송 시간 업데이트
     */
    public void updateLastPushTime() {
        this.lastPushAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        this.updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    /**
     * 구독 정보 생성 팩토리 메서드
     */
    public static WebPushSubscription of(Long userId, String endpoint, String p256dhKey, String authKey, String userAgent) {
        WebPushSubscription subscription = new WebPushSubscription();
        subscription.setUserId(userId);
        subscription.setEndpoint(endpoint);
        subscription.setP256dhKey(p256dhKey);
        subscription.setAuthKey(authKey);
        subscription.setUserAgent(userAgent);
        subscription.setIsActive(true);
        subscription.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        return subscription;
    }
}
