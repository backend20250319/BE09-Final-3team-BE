package site.petful.healthservice.medical.medication.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.petful.healthservice.schedule.enums.ScheduleSubType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MedicationUpdateRequestDTO {

    // 약물 정보 (선택적)
    private String medicationName;
    private String dosage;
    private String administration;
    
    // 일정 정보 (선택적)
    private String frequency;
    private Integer durationDays;
    private LocalDate startDate;
    private List<LocalTime> times;
    private ScheduleSubType subType;
    
    // 알림 설정 (선택적)
    @Min(value = 0, message = "알림 시기는 0~3 사이여야 합니다.")
    @Max(value = 3, message = "알림 시기는 0~3 사이여야 합니다.")
    private Integer reminderDaysBefore;
}
