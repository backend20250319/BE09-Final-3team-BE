package site.petful.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import site.petful.notificationservice.entity.Notification;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponseDto {
    
    private List<NotificationResponseDto> notifications;
    private PageInfo pageInfo;
    
    // Page<Notification>에서 DTO로 변환하는 정적 팩토리 메서드
    public static NotificationListResponseDto from(Page<Notification> notificationPage) {
        List<NotificationResponseDto> notifications = notificationPage.getContent()
                .stream()
                .map(NotificationResponseDto::from)
                .collect(Collectors.toList());
        
        PageInfo pageInfo = PageInfo.builder()
                .pageNumber(notificationPage.getNumber())
                .pageSize(notificationPage.getSize())
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .hasNext(notificationPage.hasNext())
                .hasPrevious(notificationPage.hasPrevious())
                .build();
        
        return NotificationListResponseDto.builder()
                .notifications(notifications)
                .pageInfo(pageInfo)
                .build();
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}
