package site.petful.notificationservice.messaging;


import site.petful.notificationservice.application.IdempotencyService;
import site.petful.notificationservice.application.NotificationWriteService;
import site.petful.notificationservice.dto.EventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventConsumer {
    private final NotificationWriteService notificationWriteService;
    private final IdempotencyService idempotencyService;

    @RabbitListener(queues = "${app.messaging.queue}")
    public void onEvent(EventMessage eventMessage){
        String idemKey = eventMessage.eventId();
        if (idemKey == null || idemKey.isBlank()) {
            log.warn("Received event with null or blank eventId: {}", eventMessage);
            return;
        }
        if (!idempotencyService.tryAcquire(idemKey, Duration.ofHours(12))) {
            log.info("Duplicate event dropped: {}", idemKey);
            return;
        }
        notificationWriteService.createFromEvent(eventMessage);
    }
}
