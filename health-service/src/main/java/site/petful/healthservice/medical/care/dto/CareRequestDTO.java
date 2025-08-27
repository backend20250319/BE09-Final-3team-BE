package site.petful.healthservice.medical.care.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import site.petful.healthservice.medical.care.enums.CareFrequency;
import site.petful.healthservice.schedule.dto.ScheduleRequestDTO;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CareRequestDTO extends ScheduleRequestDTO {

    // 알림 on/off. 기본 on
    private final Boolean alarmEnabled = true;
    
    // 돌봄 빈도 (CareFrequency enum 사용)
    private CareFrequency careFrequency;
}


