package site.petful.healthservice.medical.care.dto;

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
    private Long scheduleNo;
    private String title;
    private String mainType;
    private String subType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<LocalTime> times;
    private String frequency; 
    private Boolean alarmEnabled;
    private Integer reminderDaysBefore;
    private Integer lastReminderDaysBefore;
}


