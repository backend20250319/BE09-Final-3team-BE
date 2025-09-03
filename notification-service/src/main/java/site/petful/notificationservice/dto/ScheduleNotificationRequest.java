package site.petful.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleNotificationRequest {

    private Long userId;
    private Long scheduleId;
    private String scheduleTitle;
    private String scheduleType; // MEDICATION, CARE, VACCINATION, EXERCISE, TRAINING
    private LocalDateTime scheduledAt; // 알림이 발생해야 할 시간
    private Integer reminderDaysBefore; // n일 전 알림 (0 = 당일, 1 = 1일전, 2 = 2일전, 7 = 7일전)
    private String message; // 알림 메시지
    private String priority; // LOW, NORMAL, HIGH, URGENT
}
