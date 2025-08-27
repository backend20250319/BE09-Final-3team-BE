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
import site.petful.healthservice.medical.medication.schedule.dto.ScheduleRequestDTO;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class MedicationRequestDTO extends ScheduleRequestDTO {

    @NotBlank(message = "약 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "용량은 필수입니다.")
    private String amount;

    @NotBlank(message = "복용법은 필수입니다.")
    private String instruction;

    @NotNull(message = "복용 빈도는 필수입니다.")
    private MedicationFrequency medicationFrequency;

    @NotNull(message = "복용 기간(일)은 필수입니다.")
    @Min(value = 1, message = "복용 기간은 1일 이상이어야 합니다.")
    @Max(value = 365, message = "복용 기간은 365일 이하여야 합니다.")
    private Integer durationDays;


}
