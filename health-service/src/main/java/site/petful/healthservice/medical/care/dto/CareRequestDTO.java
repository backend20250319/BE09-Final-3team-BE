package site.petful.healthservice.medical.care.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import site.petful.healthservice.medical.care.enums.CareFrequency;
import site.petful.healthservice.medical.schedule.enums.ScheduleSubType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CareRequestDTO {

    @NotNull(message = "반려동물 번호는 필수입니다")
    private Long petNo;
    
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    
    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @NotNull(message = "서브타입은 필수입니다.")
    private ScheduleSubType subType;
    
    @NotNull(message = "시간은 필수입니다.")
    private List<LocalTime> times;
    
    @NotNull(message = "돌봄 빈도는 필수입니다.")
    private CareFrequency careFrequency;
    
    private Integer reminderDaysBefore;
    
    // 알림 on/off. 기본 on
    private final Boolean alarmEnabled = true;
}


