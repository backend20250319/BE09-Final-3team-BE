package site.petful.healthservice.medical.dto;

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
public class MedicationDetailDTO {
    private Long calNo;
    private String title;           // 약명 + 용량 표기
    private String mainType;        // MEDICATION
    private String subType;         // PILL|SUPPLEMENT
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalTime time;         // 대표 시간(startDate 기준)
    private String frequency;       // 라벨 저장값
    private Boolean alarmEnabled;
    private List<Integer> reminderDaysBefore;

    // 상세(Detail)
    private String medicationName;
    private String dosage;
    private Integer durationDays;
    private String instructions;
}


