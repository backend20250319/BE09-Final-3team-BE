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
                message.getTarget() != null && !message.getTarget().isEmpty() ? message.getTarget().get(0).getResourceType() : "N/A"
        );

        try {
            // health.schedule 메시지인 경우 즉시 알림 + 예약 알림 생성
            if ("health.schedule".equals(message.getType())) {
                // 1. 즉시 등록 알림 생성
                EventMessage enrollMessage = createEnrollMessage(message);
                Notification enrollNotification = notificationService.createImmediateNotification(enrollMessage);
                log.info("✅ [NotificationConsumer] 등록 알림 생성: notificationId={}", enrollNotification.getId());
                
                // 2. 스케줄 정보 파싱
                Map<String, Object> attributes = message.getAttributes();
                String startDateStr = (String) attributes.get("startDate");
                Integer reminderDaysBefore = (Integer) attributes.get("reminderDaysBefore");
                Integer durationDays = (Integer) attributes.get("durationDays");
                String scheduleTitle = (String) attributes.get("title");
                String subType = (String) attributes.get("subType");
                @SuppressWarnings("unchecked")
                List<String> times = (List<String>) attributes.get("times");
                
                log.info("🔍 [NotificationConsumer] 스케줄 정보 파싱: startDate={}, reminderDaysBefore={}, durationDays={}, title={}, subType={}, times={}", 
                        startDateStr, reminderDaysBefore, durationDays, scheduleTitle, subType, times);
                
                // times 리스트 상세 로그
                if (times != null) {
                    for (int i = 0; i < times.size(); i++) {
                        log.info("🔍 [NotificationConsumer] times[{}] = '{}'", i, times.get(i));
                    }
                }
                
                // 3. 스케줄 시작 날짜 파싱
                log.info("🔍 [NotificationConsumer] startDateStr 파싱 시작: startDateStr='{}'", startDateStr);
                LocalDateTime startDate;
                if (startDateStr.contains("T")) {
                    // 이미 완전한 날짜시간 문자열인 경우
                    startDate = LocalDateTime.parse(startDateStr);
                    log.info("🔍 [NotificationConsumer] T 포함 파싱: startDate={}", startDate);
                } else {
                    // 날짜만 있는 경우 시간 추가
                    String dateTimeStr = startDateStr + "T00:00:00";
                    log.info("🔍 [NotificationConsumer] T 추가 후 문자열: '{}'", dateTimeStr);
                    startDate = LocalDateTime.parse(dateTimeStr);
                    log.info("🔍 [NotificationConsumer] T 추가 파싱: startDate={}", startDate);
                }
                
                log.info("🔍 [NotificationConsumer] 최종 파싱된 시작 날짜: startDate={}, 현재 시간={}", startDate, LocalDateTime.now());
                
                // 4. reminderDaysBefore에 따른 알림 생성 로직
                if (durationDays == null || durationDays == 0) {
                    log.warn("⚠️ [NotificationConsumer] durationDays가 0입니다. 예약 알림을 생성하지 않습니다.");
                    return;
                }
                
                if (times == null || times.isEmpty()) {
                    log.warn("⚠️ [NotificationConsumer] times가 null이거나 비어있습니다.");
                    return;
                }
                
                // reminderDaysBefore 기본값 설정
                if (reminderDaysBefore == null) {
                    reminderDaysBefore = 0;
                }
                
                log.info("🔍 [NotificationConsumer] 알림 생성 로직: durationDays={}, reminderDaysBefore={}", 
                        durationDays, reminderDaysBefore);
                
                if (reminderDaysBefore == 0) {
                    // 당일 알림
                    createSameDayNotifications(message, scheduleTitle, subType, startDate, durationDays, times);
                        } else {
                    // 사전 알림 (자정으로 설정)
                    createAdvanceNotifications(message, scheduleTitle, subType, startDate, durationDays, times, reminderDaysBefore);
                }
            } else if ("campaign.applicant.selected".equals(message.getType())) {
                handleCampaignSelectionNotification(message);
                
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

    private void handleCampaignSelectionNotification(EventMessage message) {
        try {
            // 1. 광고 정보 추출
            String adTitle = message.getActor().getName();
            
            // 2. 선정된 지원자 정보 추출 (advertiser-service에서 개별 이벤트로 발송)
            // Target이 List이므로 첫 번째 요소 사용
            if (message.getTarget() == null || message.getTarget().isEmpty()) {
                log.error("❌ [NotificationConsumer] Target이 null이거나 비어있습니다.");
                return;
            }
            EventMessage.Target target = message.getTarget().get(0);
            Long userId = Long.parseLong(target.getUserId());
            Long applicantNo = target.getResourceId();
            
            // 3. attributes에서 추가 정보 추출
            Map<String, Object> attributes = message.getAttributes();
            String petName = (String) attributes.get("petName");
            String campaignStart = (String) attributes.get("campaignStart");
            String campaignEnd = (String) attributes.get("campaignEnd");
            
            // 4. 알림 메시지 생성
            EventMessage notificationMessage = createCampaignSelectionNotificationMessage(
                    message, userId, applicantNo, adTitle, petName, campaignStart, campaignEnd
            );
            
            // 5. 알림 저장
            Notification notification = notificationService.createImmediateNotification(notificationMessage);
            
            log.info("✅ [NotificationConsumer] 체험단 선정 알림 생성 성공: userId={}, applicantNo={}, notificationId={}",
                    userId, applicantNo, notification.getId());
                    
        } catch (Exception e) {
            log.error("❌ [NotificationConsumer] 체험단 선정 알림 처리 실패: eventId={}, error={}",
                    message.getEventId(), e.getMessage(), e);
            throw e;
        }
    }

    private EventMessage createCampaignSelectionNotificationMessage(
            EventMessage originalMessage, Long userId, Long applicantNo, String adTitle, 
            String petName, String campaignStart, String campaignEnd
    ) {
        EventMessage notificationMessage = new EventMessage();
        notificationMessage.setEventId(java.util.UUID.randomUUID().toString());
        notificationMessage.setType("campaign.applicant.selected");
        notificationMessage.setOccurredAt(java.time.Instant.now());
        notificationMessage.setSchemaVersion(1);

        // Actor 설정 (광고 정보)
        notificationMessage.setActor(originalMessage.getActor());

        // Target 설정 (선정된 사용자) - List로 설정
        EventMessage.Target target = new EventMessage.Target();
        target.setUserId(String.valueOf(userId));
        target.setResourceId(applicantNo);
        target.setResourceType("CAMPAIGN");
        notificationMessage.setTarget(java.util.Arrays.asList(target));

        // 알림 속성 설정
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("title", "🎉 체험단 선정 알림");
        attributes.put("message", createCampaignSelectionMessage(adTitle, petName, campaignStart, campaignEnd));
        attributes.put("adTitle", adTitle);
        attributes.put("petName", petName);
        attributes.put("applicantNo", applicantNo);
        attributes.put("campaignStart", campaignStart);
        attributes.put("campaignEnd", campaignEnd);
        notificationMessage.setAttributes(attributes);

        return notificationMessage;
    }

    private String createCampaignSelectionMessage(String adTitle, String petName, String campaignStart, String campaignEnd) {
        return String.format("🎉 축하합니다! [%s] 체험단에 선정되셨습니다.\n\n" +
                "🐾 반려동물: %s\n" +
                "📅 캠페인 기간: %s ~ %s\n\n" +
                "자세한 내용은 해당 페이지에서 확인해주세요.", 
                adTitle, petName, campaignStart, campaignEnd);
    }

    /**
     * 등록 알림 메시지 생성
     */
    private EventMessage createEnrollMessage(EventMessage originalMessage) {
        EventMessage enrollMessage = new EventMessage();
        enrollMessage.setEventId(java.util.UUID.randomUUID().toString());
        enrollMessage.setType("health.schedule");
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
     * 당일 알림 생성 (reminderDaysBefore = 0)
     */
    private void createSameDayNotifications(EventMessage originalMessage, String scheduleTitle, String subType,
                                          LocalDateTime startDate, Integer durationDays, List<String> times) {
        log.info("🔍 [NotificationConsumer] 당일 알림 생성 시작: startDate={}, durationDays={}, subType={}", startDate, durationDays, subType);
        
        LocalDateTime now = LocalDateTime.now();
        log.info("🔍 [NotificationConsumer] 현재 시간: {}", now);
        
        for (int day = 0; day < durationDays; day++) {
            LocalDateTime currentDate = startDate.plusDays(day);
            
            for (String timeStr : times) {
                LocalDateTime scheduledTime = parseTimeToDateTime(currentDate, timeStr);
                
                // 현재 시간이 이미 지났는지 확인
                if (scheduledTime.isBefore(now)) {
                    log.info("⏰ [NotificationConsumer] 시간이 이미 지남 - 알림 생성 건너뜀: scheduledTime={}, now={}", scheduledTime, now);
                    continue;
                }
                
                // subType에 따른 메시지 생성
                String message = createSameDayMessage(timeStr, scheduleTitle, subType);
                
                EventMessage reserveMessage = createReserveMessage(originalMessage, scheduleTitle, scheduledTime, message);
                Notification reserveNotification = notificationService.createScheduledNotification(reserveMessage, scheduledTime);
                
                log.info("✅ [NotificationConsumer] 당일 알림 생성: notificationId={}, scheduledTime={}, message={}", 
                        reserveNotification.getId(), scheduledTime, message);
            }
        }
    }
    
    /**
     * 사전 알림 생성 (reminderDaysBefore > 0) - 자정으로 설정
     */
    private void createAdvanceNotifications(EventMessage originalMessage, String scheduleTitle, String subType,
                                          LocalDateTime startDate, Integer durationDays, List<String> times, Integer reminderDaysBefore) {
        log.info("🔍 [NotificationConsumer] 사전 알림 생성 시작: startDate={}, durationDays={}, reminderDaysBefore={}, subType={}", 
                startDate, durationDays, reminderDaysBefore, subType);
        
        // 시작일 + reminderDaysBefore부터 duration 기간까지
        LocalDateTime notificationStartDate = startDate.plusDays(reminderDaysBefore);
        LocalDateTime notificationEndDate = startDate.plusDays(durationDays);
        
        log.info("🔍 [NotificationConsumer] 알림 기간: {} ~ {}", notificationStartDate, notificationEndDate);
        
        for (int day = 0; day < durationDays; day++) {
            LocalDateTime currentDate = startDate.plusDays(day);
            LocalDateTime notificationDate = currentDate.plusDays(reminderDaysBefore);
            
            // 알림 기간 내에 있는지 확인
            if (notificationDate.isBefore(notificationStartDate) || notificationDate.isAfter(notificationEndDate)) {
                continue;
            }
            
            // 사전 알림은 자정(00:00)으로 설정
            LocalDateTime scheduledTime = notificationDate.withHour(0).withMinute(0).withSecond(0);
            
            // subType에 따른 사전 알림 메시지 생성 (실제 times 사용)
            String message = createAdvanceMessage(reminderDaysBefore, times, scheduleTitle, subType);
            
            EventMessage reserveMessage = createReserveMessage(originalMessage, scheduleTitle, scheduledTime, message);
            Notification reserveNotification = notificationService.createScheduledNotification(reserveMessage, scheduledTime);
            
            log.info("✅ [NotificationConsumer] 사전 알림 생성 (자정): notificationId={}, scheduledTime={}, message={}", 
                    reserveNotification.getId(), scheduledTime, message);
        }
    }
    
    /**
     * 당일 알림 메시지 생성
     */
    private String createSameDayMessage(String timeStr, String scheduleTitle, String subType) {
        log.info("🔍 [createSameDayMessage] 파라미터: timeStr={}, scheduleTitle={}, subType={}", timeStr, scheduleTitle, subType);
        
        if ("PILL".equals(subType)) {
            // 복용약/영양제: "지금 08:00, 약이름 복용 시간입니다"
            String message = String.format("지금 %s, %s 복용 시간입니다", timeStr, scheduleTitle);
            log.info("🔍 [createSameDayMessage] PILL 메시지 생성: {}", message);
            return message;
        } else {
            // 돌봄/산책 등: "지금 08:00 아침 산책 시간입니다"
            String message = String.format("지금 %s %s 시간입니다", timeStr, scheduleTitle);
            log.info("🔍 [createSameDayMessage] 기타 메시지 생성: {}", message);
            return message;
        }
    }
    
    /**
     * 사전 알림 메시지 생성
     */
    private String createAdvanceMessage(Integer reminderDaysBefore, List<String> times, String scheduleTitle, String subType) {
        // times를 문자열로 변환 (예: "08:00, 12:00, 20:00")
        String timesStr = String.join(", ", times);
        
        if ("PILL".equals(subType)) {
            // 복용약/영양제: "1일후 08:00에 약이름 복용 예정입니다"
            return String.format("%d일후 %s에 %s 복용 예정입니다.", reminderDaysBefore, timesStr, scheduleTitle);
        } else {
            // 돌봄/산책 등: "1일후 08:00에 아침 산책 예정입니다"
            return String.format("%d일후 %s에 %s 예정입니다.", reminderDaysBefore, timesStr, scheduleTitle);
        }
    }
    
    /**
     * 시간 문자열을 LocalDateTime으로 변환
     */
    private LocalDateTime parseTimeToDateTime(LocalDateTime date, String timeStr) {
        String[] timeParts = timeStr.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        
        return LocalDateTime.of(
                date.getYear(), 
                date.getMonth(), 
                date.getDayOfMonth(), 
                hour, 
                minute, 
                0
        );
    }
    
    /**
     * 예약 알림 메시지 생성 (health.schedule)
     */
    private EventMessage createReserveMessage(EventMessage originalMessage, String scheduleTitle, 
                                            LocalDateTime scheduledTime, String customMessage) {
        EventMessage reserveMessage = new EventMessage();
        reserveMessage.setEventId(java.util.UUID.randomUUID().toString());
        reserveMessage.setType("health.schedule.reserve");
        reserveMessage.setOccurredAt(java.time.Instant.now());
        reserveMessage.setActor(originalMessage.getActor());
        reserveMessage.setTarget(originalMessage.getTarget());
        
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("title", scheduleTitle);
        attributes.put("scheduledTime", scheduledTime.toString());
        attributes.put("message", customMessage);
        reserveMessage.setAttributes(attributes);
        reserveMessage.setSchemaVersion(1);
        
        return reserveMessage;
    }
}
