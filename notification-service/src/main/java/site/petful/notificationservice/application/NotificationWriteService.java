package site.petful.notificationservice.application;

import lombok.extern.slf4j.Slf4j;
import site.petful.notificationservice.dto.EventMessage;
import site.petful.notificationservice.entity.Notification;
import site.petful.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationWriteService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createFromEvent(EventMessage eventMessage){
        Notification notification = new Notification();
        notification.setUserId(Long.valueOf(eventMessage.target().userId()));
        notification.setType(eventMessage.type());
        notification.setTitle(buildTitle(eventMessage));
        notification.setContent(buildContent(eventMessage));
        notification.setLinkUrl(null);
        notification.setHidden(false);
        notification.setHiddenAt(null);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    private String buildTitle(EventMessage eventMessage) {
        return switch (eventMessage.type()) {
            case "CAMPAIGN_NEW"     -> "새 캠페인 소식";
            case "CAMPAIGN_LINKED"  -> "캠페인 연결 알림";
            case "COMMUNITY_ACTIVE" -> "커뮤니티 활동 알림";
            case "SOCIAL_GROWTH"    -> "소셜미디어 성장 알림";
            case "HEALTH_SCHEDULE"  -> "건강 일정 알림";
            default -> "알림";
        };
    }

    private String buildContent(EventMessage eventMessage) {
        Object name = eventMessage.attributes() != null ? eventMessage.attributes().get("name") : null;
        return name != null ? String.valueOf(name) : eventMessage.type();
    }
}
