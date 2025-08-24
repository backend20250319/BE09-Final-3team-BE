package site.petful.healthservice.medical.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import site.petful.healthservice.common.entity.Calendar;

@Entity
@Table(name = "calendar_med_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarMedDetail {

    @Id
    @Column(name = "cal_no")
    private Long calNo;

    @OneToOne
    @MapsId
    @JoinColumn(name = "cal_no")
    private Calendar calendar;

    @Column(name = "medication_name")
    private String medicationName;

    @Column(name = "dosage")
    private String dosage;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "ocr_raw_data", columnDefinition = "TEXT")
    private String ocrRawData;
}


