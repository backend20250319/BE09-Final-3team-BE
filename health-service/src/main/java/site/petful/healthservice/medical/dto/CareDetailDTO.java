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
public class CareDetailDTO {
    private Long calNo;
    private String title;
    private String mainType;
    private String subType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalTime time; // 일정 시간
    private String frequency; // 라벨
    private Boolean alarmEnabled;
    private List<Integer> reminderDaysBefore; // 전체
}


