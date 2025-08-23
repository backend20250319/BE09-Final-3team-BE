package site.petful.healthservice.medical.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class MedicationRequestDTO {

    private String medicationName;
    private String dosage;
    private String administration;
    private String frequency;
    private Integer durationDays;
    private LocalDate startDate;
    private String subType;
    private Integer reminderDaysBefore;
}
