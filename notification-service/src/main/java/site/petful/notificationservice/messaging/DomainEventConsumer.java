package site.petful.notificationservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import site.petful.notificationservice.dto.EventMessage;
import site.petful.notificationservice.entity.Notification;
import site.petful.notificationservice.service.NotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${app.messaging.queue}")
    public void onMessage(EventMessage message) {
        log.info("📩 [NotificationConsumer] 받은 메시지: eventId={}, type={}, actor={}, target={}",
                message.getEventId(),
                message.getType(),
                message.getActor() != null ? message.getActor().getName() : "N/A",
                message.getTarget() != null ? message.getTarget().getResourceType() : "N/A"
        );

        try {
            // 이벤트 메시지를 받아서 알림 저장
            Notification savedNotification = notificationService.createImmediateNotification(message);
            log.info("✅ [NotificationConsumer] 알림 저장 성공: notificationId={}", savedNotification.getId());
        } catch (Exception e) {
            log.error("❌ [NotificationConsumer] 알림 저장 실패: eventId={}, error={}", message.getEventId(), e.getMessage(), e);
            throw e; // 메시지 재처리를 위해 예외를 다시 던짐
        }
    }
}
