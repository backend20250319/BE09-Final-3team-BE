package site.petful.healthservice.medical.care.dto;

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

import site.petful.healthservice.common.enums.CalendarSubType;
import site.petful.healthservice.common.enums.CareFrequency;

@Getter
@Setter
@NoArgsConstructor
public class CareRequestDTO {

    // 일정 이름
    @NotBlank(message = "일정 이름은 필수입니다.")
    private String title;

    // 서브타입: WALK, GROOMING, BIRTHDAY, ETC
    @NotNull(message = "유형(서브타입)은 필수입니다.")
    private CalendarSubType subType;

    // 빈도 라벨: 매일, 매주, 매월, 연 1회, 반년 1회, 월 1회, 주 1회 등
    @NotNull(message = "빈도는 필수입니다.")
    private CareFrequency frequency;

    // 시작/종료 날짜
    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDate endDate;

    // 일정 시간들 (하루 한 번 기준이지만 List로 통일)
    @NotNull(message = "일정 시간들은 필수입니다.")
    private List<LocalTime> times;

    // 알림 시기: 0=당일, 1/2/3일 전
    @NotNull(message = "알림 시기는 필수입니다.")
    @Min(value = 0, message = "알림 시기는 0~3 사이여야 합니다.")
    @Max(value = 3, message = "알림 시기는 0~3 사이여야 합니다.")
    private Integer reminderDaysBefore;

    // 알림 on/off. 기본 on
    private Boolean alarmEnabled = true;
}


