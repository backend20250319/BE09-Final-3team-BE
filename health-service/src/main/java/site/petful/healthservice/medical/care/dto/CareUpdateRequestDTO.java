package site.petful.healthservice.medical.care.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.petful.healthservice.common.enums.CalendarSubType;
import site.petful.healthservice.common.enums.CareFrequency;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CareUpdateRequestDTO {
    private String title;                 // 일정 이름 (옵션)
    private CalendarSubType subType;      // WALK/GROOMING/BIRTHDAY/ETC (옵션)
    private CareFrequency frequency;      // 빈도 (옵션)
    private LocalDate startDate;          // 시작일 (옵션)
    private LocalDate endDate;            // 종료일 (옵션)
    private List<LocalTime> times;       // 일정 시간들 (옵션)
    private Integer reminderDaysBefore;   // 0/1/2/3 (옵션)
    private Boolean alarmEnabled;         // on/off (옵션)
}


