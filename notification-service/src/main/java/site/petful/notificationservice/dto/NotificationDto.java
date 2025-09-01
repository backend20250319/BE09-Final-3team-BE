package site.petful.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import site.petful.notificationservice.entity.Notification;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class NotificationDto {
    Long id;
    String type;
    String title;
    String content;
    String linkUrl;
    LocalDateTime createdAt;

    public static NotificationDto from(Notification n){
        return new NotificationDto(
                n.getId(), n.getType(), n.getTitle(), n.getContent(),
                n.getLinkUrl(), n.getCreatedAt()
                );
    }
}
