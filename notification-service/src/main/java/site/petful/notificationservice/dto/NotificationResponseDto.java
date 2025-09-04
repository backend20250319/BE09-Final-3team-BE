package site.petful.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.notificationservice.entity.Notification;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
    
    private Long id;
    private String type;
    private String title;
    private String content;
    private String linkUrl;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private Notification.NotificationStatus status;
    private Boolean isRead;
    private String relativeTime;  
    
    public static NotificationResponseDto from(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .linkUrl(notification.getLinkUrl())
                .createdAt(notification.getCreatedAt())
                .scheduledAt(notification.getScheduledAt())
                .sentAt(notification.getSentAt())
                .status(notification.getStatus())
                .isRead(notification.getIsRead())
                .relativeTime(calculateRelativeTime(notification))
                .build();
    }

    private static String calculateRelativeTime(Notification notification) {
        // sentAt이 있으면 sentAt 기준으로, 없으면 createdAt 기준으로 계산
        LocalDateTime baseTime = notification.getSentAt() != null ? 
            notification.getSentAt() : notification.getCreatedAt();
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(baseTime, now);
        long hours = ChronoUnit.HOURS.between(baseTime, now);
        long days = ChronoUnit.DAYS.between(baseTime, now);

        if (minutes < 1) {
            return "방금전";
        } else if (minutes < 60) {
            return minutes + "분전";
        } else if (hours < 24) {
            return hours + "시간전";
        } else if (days < 7) {
            return days + "일전";
        } else {
            return baseTime.toLocalDate().toString();
        }
    }
}
