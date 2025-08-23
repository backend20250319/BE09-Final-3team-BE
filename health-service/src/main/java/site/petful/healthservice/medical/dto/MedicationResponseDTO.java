package site.petful.healthservice.medical.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationResponseDTO {
    private Long calNo;
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
    private String instructions; // 용법/지시사항
}
