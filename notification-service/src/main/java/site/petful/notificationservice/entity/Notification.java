package site.petful.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name="Notifications")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_no")
    private Long id;

    @Column(name="user_no",nullable = false)
    private Long userId;

    @Column(nullable = false , length = 32)
    private String type;

    @Column(length = 120)
    private String title;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "link_url", length = 512)
    private String linkUrl;

    @Column(name = "is_hidden", nullable = false)
    private Boolean hidden = false;              // TINYINT(1)

    @Column(name = "hidden_at")
    private LocalDateTime hiddenAt;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;              // 읽음 여부

    @Column(name = "read_at")
    private LocalDateTime readAt;                // 읽은 시간

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;  // 예약 알림 시간

    @Column(name = "sent_at")
    private LocalDateTime sentAt;       // 실제 발송 시간

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;  // 알림 상태

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        }
        // scheduledAt이 있다면 시간대 변환 없이 그대로 유지
        if (scheduledAt != null) {
            System.out.println("🔍 [Notification.@PrePersist] scheduledAt 저장 전: " + scheduledAt);
            // scheduledAt은 그대로 유지 (시간대 변환 없음)
        }
    }

    public void hide() {
        if (Boolean.TRUE.equals(this.hidden)) return;
        this.hidden = true;
        this.hiddenAt = LocalDateTime.now();
    }


    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = NotificationStatus.FAILED;
    }

    public void markAsRead() {
        if (Boolean.TRUE.equals(this.isRead)) return;
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public static Notification of(Long userId, String type, String title, String content, String linkUrl) {
        return new Notification(null, userId, type, title, content, linkUrl, false, null, false, null, LocalDateTime.now(ZoneId.of("Asia/Seoul")), null, null, NotificationStatus.PENDING);
    }

    public static Notification scheduled(Long userId, String type, String title, String content, String linkUrl, LocalDateTime scheduledAt) {
        System.out.println("🔍 [Notification.scheduled] scheduledAt 파라미터: " + scheduledAt);
        System.out.println("🔍 [Notification.scheduled] scheduledAt 상세: year=" + scheduledAt.getYear() + 
                ", month=" + scheduledAt.getMonth() + ", day=" + scheduledAt.getDayOfMonth() + 
                ", hour=" + scheduledAt.getHour() + ", minute=" + scheduledAt.getMinute() + ", second=" + scheduledAt.getSecond());
        
        Notification notification = new Notification(null, userId, type, title, content, linkUrl, false, null, false, null, LocalDateTime.now(ZoneId.of("Asia/Seoul")), scheduledAt, null, NotificationStatus.SCHEDULED);
        
        System.out.println("🔍 [Notification.scheduled] 생성된 엔티티의 scheduledAt: " + notification.getScheduledAt());
        
        return notification;
    }

    public enum NotificationStatus {
        PENDING,    // 대기 중 (즉시 발송)
        SCHEDULED,  // 예약됨
        SENT,       // 발송 완료
        FAILED      // 발송 실패
    }
}
