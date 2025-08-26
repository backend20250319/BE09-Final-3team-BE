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
    
    // 시간 관련 필드 추가
    private LocalTime time;         // 대표 시간(startDate 기준)
    private List<LocalTime> times;  // 하루 N회일 때 전개 시간 목록
}
