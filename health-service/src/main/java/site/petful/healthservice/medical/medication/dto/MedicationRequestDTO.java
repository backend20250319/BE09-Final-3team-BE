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
    
    @NotNull(message = "복용 기간(일)은 필수입니다.")
    @Min(value = 1, message = "복용 기간은 1일 이상이어야 합니다.")
    @Max(value = 365, message = "복용 기간은 365일 이하여야 합니다.")
    private Integer durationDays;
    
    @NotNull(message = "복용 빈도는 필수입니다.")
    private MedicationFrequency medicationFrequency;
    
    @NotNull(message = "시간은 필수입니다.")
    private List<LocalTime> times;
    
    private Integer reminderDaysBefore;  // null이면 기본값(0) 사용
}
