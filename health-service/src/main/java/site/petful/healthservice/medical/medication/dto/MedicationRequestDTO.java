package site.petful.healthservice.medical.medication.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import site.petful.healthservice.medical.medication.enums.MedicationFrequency;
import site.petful.healthservice.medical.schedule.enums.ScheduleSubType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class MedicationRequestDTO {

    @NotNull(message = "반려동물 번호는 필수입니다")
    private Long petNo;
    
    @NotBlank(message = "약 이름은 필수입니다.")
    private String name;
    
    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;
    
    @NotNull(message = "종료일은 필수입니다.")
    private LocalDate endDate;
    
    @NotNull(message = "복용 빈도는 필수입니다.")
    private MedicationFrequency medicationFrequency;
    
    private List<LocalTime> times;  // 선택적: 제공되지 않으면 빈도에 따라 기본 시간 사용
    
    private ScheduleSubType subType;  // 영양제/복용약 구분 (기본값: PILL)
    
    private Boolean isPrescription;  // 처방전 여부 (기본값: false)
    
    private Integer reminderDaysBefore;  // null이면 기본값(0) 사용
}
