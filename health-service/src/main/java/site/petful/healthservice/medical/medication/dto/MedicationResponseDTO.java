package site.petful.healthservice.medical.medication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationResponseDTO {
    private Long scheduleNo;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime alarmTime;
    private String mainType;
    private String subType;

    private String medicationName;
    private String dosage;
    private String frequency;
    private Integer durationDays;
    
    // 시간 관련 필드 추가
    private LocalTime time;         // 대표 시간(startDate 기준)
    private List<LocalTime> times;  // 하루 N회일 때 전개 시간 목록
    
    // 알림 관련 필드
    private Integer reminderDaysBefore;  // 0: 당일, 1: 1일전, 2: 2일전, 3: 3일전
}
