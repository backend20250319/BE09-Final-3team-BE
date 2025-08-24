package site.petful.healthservice.medical.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MedicationRequestDTO {

    @NotBlank(message = "약 이름은 필수입니다.")
    private String medicationName;

    @NotBlank(message = "용량은 필수입니다.")
    private String dosage;

    @NotBlank(message = "복용법은 필수입니다.")
    private String administration;

    @NotBlank(message = "복용 빈도는 필수입니다.")
    private String frequency;

    @NotNull(message = "복용 기간(일)은 필수입니다.")
    @Min(value = 1, message = "복용 기간은 1일 이상이어야 합니다.")
    @Max(value = 365, message = "복용 기간은 365일 이하여야 합니다.")
    private Integer durationDays;

    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDate startDate;

    @NotBlank(message = "유형(서브타입)은 필수입니다.")
    private String subType;

    @NotNull(message = "알림 시기는 필수입니다.")
    @Min(value = 0, message = "알림 시기는 0~3 사이여야 합니다.")
    @Max(value = 3, message = "알림 시기는 0~3 사이여야 합니다.")
    private Integer reminderDaysBefore;

    // 일정 시간들 (필수사항 - 하루 N회에 맞는 시간 개수)
    @NotNull(message = "일정 시간들은 필수입니다.")
    private List<LocalTime> times;
}
