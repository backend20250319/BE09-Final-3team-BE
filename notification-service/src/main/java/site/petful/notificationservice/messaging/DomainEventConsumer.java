package site.petful.notificationservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import site.petful.notificationservice.dto.EventMessage;
import site.petful.notificationservice.entity.Notification;
import site.petful.notificationservice.service.NotificationService;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

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
            // health.schedule 메시지인 경우 즉시 알림 + 예약 알림 생성
            if ("health.schedule".equals(message.getType())) {
                // 1. 즉시 등록 알림 생성 (health.schedule.enroll)
                EventMessage enrollMessage = createEnrollMessage(message);
                Notification enrollNotification = notificationService.createImmediateNotification(enrollMessage);
                log.info("✅ [NotificationConsumer] 등록 알림 생성: notificationId={}", enrollNotification.getId());
                
                // 2. 스케줄 정보 파싱
                Map<String, Object> attributes = message.getAttributes();
                String startDateStr = (String) attributes.get("startDate");
                Integer reminderDaysBefore = (Integer) attributes.get("reminderDaysBefore");
                Integer durationDays = (Integer) attributes.get("durationDays");
                String scheduleTitle = (String) attributes.get("title");
                List<String> times = (List<String>) attributes.get("times");
                
                log.info("🔍 [NotificationConsumer] 스케줄 정보 파싱: startDate={}, reminderDaysBefore={}, durationDays={}, title={}, times={}", 
                        startDateStr, reminderDaysBefore, durationDays, scheduleTitle, times);
                
                // 3. 스케줄 시작 날짜 파싱
                LocalDateTime startDate;
                if (startDateStr.contains("T")) {
                    // 이미 완전한 날짜시간 문자열인 경우
                    startDate = LocalDateTime.parse(startDateStr);
                } else {
                    // 날짜만 있는 경우 시간 추가
                    startDate = LocalDateTime.parse(startDateStr + "T00:00:00");
                }
                
                log.info("🔍 [NotificationConsumer] 파싱된 시작 날짜: startDate={}, 현재 시간={}", startDate, LocalDateTime.now());
                
                // 4. durationDays 동안 매일 times에 맞춰 예약 알림 생성
                if (durationDays == null) {
                    log.warn("⚠️ [NotificationConsumer] durationDays가 null입니다. 예약 알림을 생성하지 않습니다.");
                    return;
                }
                
                // durationDays 동안 알림 생성 (reminderDaysBefore는 사전 알림 시간을 결정하는 용도)
                int actualDurationDays = durationDays;
                
                log.info("🔍 [NotificationConsumer] 알림 생성 기간 계산: durationDays={}, reminderDaysBefore={}, actualDurationDays={}", 
                        durationDays, reminderDaysBefore, actualDurationDays);
                
                for (int day = 0; day < actualDurationDays; day++) {
                    LocalDateTime currentDate = startDate.plusDays(day);
                    
                    if (times == null || times.isEmpty()) {
                        log.warn("⚠️ [NotificationConsumer] times가 null이거나 비어있습니다. day={}", day);
                        continue;
                    }
                    
                    log.info("🔍 [NotificationConsumer] {}일차 처리 중: currentDate={}", day, currentDate);
                    
                    for (String timeStr : times) {
                        // 시간 파싱 (예: "09:00:00")
                        String[] timeParts = timeStr.split(":");
                        int hour = Integer.parseInt(timeParts[0]);
                        int minute = Integer.parseInt(timeParts[1]);
                        
                        LocalDateTime scheduledTime = currentDate.withHour(hour).withMinute(minute).withSecond(0);
                        
                        log.info("🔍 [NotificationConsumer] 스케줄 시간 계산: timeStr={}, scheduledTime={}, 현재시간={}, isAfter={}", 
                                timeStr, scheduledTime, LocalDateTime.now(), scheduledTime.isAfter(LocalDateTime.now()));
                        
                        // 1. 예약 알림 생성 (health.schedule.reserve)
                        long delayMinutes = java.time.Duration.between(LocalDateTime.now(), scheduledTime).toMinutes();
                        log.info("🔍 [NotificationConsumer] 예약 알림 delayMinutes 계산: {}분", delayMinutes);
                        
                        if (delayMinutes > 0) {
                            EventMessage reserveMessage = createReserveMessage(message, scheduleTitle, scheduledTime);
                            Notification reserveNotification = notificationService.createScheduledNotification(reserveMessage, (int) delayMinutes);
                            
                            log.info("✅ [NotificationConsumer] 예약 알림 생성: notificationId={}, scheduledAt={}", 
                                    reserveNotification.getId(), reserveNotification.getScheduledAt());
                        } else {
                            log.warn("⚠️ [NotificationConsumer] 예약 알림 delayMinutes가 0 이하입니다. 생성하지 않습니다. delayMinutes={}, scheduledTime={}, now={}", 
                                    delayMinutes, scheduledTime, LocalDateTime.now());
                        }
                    }
                }
            } else {
                // 기타 메시지는 즉시 알림 생성
                Notification savedNotification = notificationService.createImmediateNotification(message);
                log.info("✅ [NotificationConsumer] 즉시 알림 저장 성공: notificationId={}", savedNotification.getId());
            }
        } catch (Exception e) {
            log.error("❌ [NotificationConsumer] 알림 저장 실패: eventId={}, error={}", message.getEventId(), e.getMessage(), e);
            throw e; // 메시지 재처리를 위해 예외를 다시 던짐
        }
    }
    
    /**
     * 등록 알림 메시지 생성 (health.schedule.enroll)
     */
    private EventMessage createEnrollMessage(EventMessage originalMessage) {
        EventMessage enrollMessage = new EventMessage();
        enrollMessage.setEventId(java.util.UUID.randomUUID().toString());
        enrollMessage.setType("health.schedule.enroll");
        enrollMessage.setOccurredAt(java.time.Instant.now());
        enrollMessage.setActor(originalMessage.getActor());
        enrollMessage.setTarget(originalMessage.getTarget());
        
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("title", "새로운 건강 일정");
        attributes.put("message", "새로운 건강 일정이 등록되었습니다.");
        enrollMessage.setAttributes(attributes);
        enrollMessage.setSchemaVersion(1);
        
        return enrollMessage;
    }
    
    /**
     * 예약 알림 메시지 생성 (health.schedule.reserve)
     */
    private EventMessage createReserveMessage(EventMessage originalMessage, String scheduleTitle, LocalDateTime scheduledTime) {
        EventMessage reserveMessage = new EventMessage();
        reserveMessage.setEventId(java.util.UUID.randomUUID().toString());
        reserveMessage.setType("health.schedule.reserve");
        reserveMessage.setOccurredAt(java.time.Instant.now());
        reserveMessage.setActor(originalMessage.getActor());
        reserveMessage.setTarget(originalMessage.getTarget());
        
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("title", scheduleTitle);
        attributes.put("scheduledTime", scheduledTime.toString());
        attributes.put("message", scheduleTitle + " 시간입니다.");
        reserveMessage.setAttributes(attributes);
        reserveMessage.setSchemaVersion(1);
        
        return reserveMessage;
    }
}
