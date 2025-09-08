package site.petful.healthservice.medical.medication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import site.petful.healthservice.connectNotice.dto.EventMessage;
import site.petful.healthservice.medical.schedule.entity.Schedule;
import site.petful.healthservice.medical.schedule.enums.ScheduleMainType;
import site.petful.healthservice.medical.medication.repository.ScheduleMedicationDetailRepository;

import java.time.Instant;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicationEventService {
    
    private final RabbitTemplate rabbitTemplate;
    private final ScheduleMedicationDetailRepository medicationDetailRepository;
    
    /**
     * 스케줄 생성 이벤트 발행
     */
    public void publishScheduleCreatedEvent(Schedule schedule) {
        try {
            EventMessage event = new EventMessage();
            event.setEventId(UUID.randomUUID().toString());
            event.setType("health.schedule");
            event.setOccurredAt(Instant.now());
            event.setActor(new EventMessage.Actor(schedule.getUserNo(), "User"));
            event.setTarget(new EventMessage.Target(
                schedule.getUserNo().toString(),
                schedule.getScheduleNo(),
                "SCHEDULE"));
            
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("scheduleNo", schedule.getScheduleNo());
            attributes.put("title", schedule.getTitle());
            attributes.put("mainType", schedule.getMainType().name());
            attributes.put("subType", schedule.getSubType().name());
            attributes.put("startDate", schedule.getStartDate());
            attributes.put("reminderDaysBefore", schedule.getReminderDaysBefore());
            
            // durationDays와 times 데이터 추가
            Integer durationDays = null;
            List<String> timesList = null;
            
                // MEDICATION 타입인 경우에만 ScheduleMedDetail에서 durationDays 가져오기
            if (schedule.getMainType() == ScheduleMainType.MEDICATION) {
                var detailOpt = medicationDetailRepository.findById(schedule.getScheduleNo());
                if (detailOpt.isPresent()) {
                    durationDays = detailOpt.get().getDurationDays();
                }
            }
            
            // times를 List<String> 형태로 변환
            if (schedule.getTimes() != null && !schedule.getTimes().isEmpty()) {
                timesList = schedule.getTimesAsList().stream()
                    .map(LocalTime::toString)
                    .toList();
            }
            
            attributes.put("durationDays", durationDays);
            attributes.put("times", timesList);
            event.setAttributes(attributes);
            event.setSchemaVersion(1);
            
            // notif.events로 메시지 발행
            rabbitTemplate.convertAndSend("notif.events", "health.schedule", event);
            
            log.info("스케줄 생성 이벤트 발행 완료: scheduleNo={}, title={}, durationDays={}, times={}", 
                    schedule.getScheduleNo(), schedule.getTitle(), durationDays, timesList);
        } catch (Exception e) {
            log.error("스케줄 생성 이벤트 발행 실패: scheduleNo={}, error={}", 
                    schedule.getScheduleNo(), e.getMessage(), e);
        }
    }
}
