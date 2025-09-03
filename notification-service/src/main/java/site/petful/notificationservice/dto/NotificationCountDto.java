package site.petful.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCountDto {
    
    private long unreadCount;  
    
    public static NotificationCountDto of(long unreadCount) {
        return NotificationCountDto.builder()
                .unreadCount(unreadCount)
                .build();
    }
}