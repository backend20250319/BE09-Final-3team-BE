package site.petful.healthservice.medical.medication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MedicationUpdateDiffDTO {
    private Snapshot before;
    private Snapshot after;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Snapshot {
        private String title;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String medicationName;
        private String dosage;
        private String frequency;
        private Integer durationDays;
        private String instructions;
        private String subType;
        private Integer reminderDaysBefore;
    }
}


