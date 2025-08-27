package site.petful.healthservice.medical.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import site.petful.healthservice.medical.schedule.enums.ScheduleSubType;
import site.petful.healthservice.medical.schedule.enums.RecurrenceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ScheduleRequestDTO {
    
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    
    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @NotNull(message = "서브타입은 필수입니다.")
    private ScheduleSubType subType;
    
    @NotNull(message = "시간은 필수입니다.")
    private List<LocalTime> times;
    
    @NotNull(message = "반복 타입은 필수입니다.")
    private RecurrenceType frequency;
    
    private Integer recurrenceInterval;
    
    private LocalDate recurrenceEndDate;
    
    private Integer reminderDaysBefore;  // null이면 자동으로 당일 알림 활성화
    
    private String frequencyText;
}
