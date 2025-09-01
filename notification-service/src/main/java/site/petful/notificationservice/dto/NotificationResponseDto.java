package site.petful.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.notificationservice.entity.Notification;

import java.time.LocalDateTime;

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
    
    // Entity에서 DTO로 변환하는 정적 팩토리 메서드
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
                .build();
    }
}
