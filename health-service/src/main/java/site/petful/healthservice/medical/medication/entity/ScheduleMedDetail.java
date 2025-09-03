package site.petful.healthservice.medical.medication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "schedule_med_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleMedDetail {

    @Id
    @Column(name = "schedule_no")
    private Long scheduleNo;


    @Column(name = "medication_name")
    private String medicationName;

    @Column(name = "dosage")
    private String dosage;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "is_prescription", nullable = false)
    @Builder.Default
    private Boolean isPrescription = false; // true: 처방전으로 등록, false: 일반 등록

    @Column(name = "ocr_raw_data", columnDefinition = "TEXT")
    private String ocrRawData;
}


