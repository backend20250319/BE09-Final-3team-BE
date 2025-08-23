package site.petful.healthservice.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.healthservice.common.enums.CalendarMainType;
import site.petful.healthservice.common.enums.CalendarSubType;
import site.petful.healthservice.common.enums.RecurrenceType;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "calendar")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Calendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cal_no")
    private Long calNo;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "main_type", nullable = false)
    private CalendarMainType mainType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_type", nullable = false)
    private CalendarSubType subType;

    @Column(name = "all_day", nullable = false)
    @Builder.Default
    private Boolean allDay = false;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "alarm_time")
    private LocalDateTime alarmTime;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "user_no", nullable = false)
    private Long userNo;

    @Column(name = "pet_no")
    private Long petNo;

    // 반복 일정 설정
    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false)
    @Builder.Default
    private RecurrenceType recurrenceType = RecurrenceType.NONE;

    @Column(name = "recurrence_interval")
    private Integer recurrenceInterval; // 간격

    @Column(name = "recurrence_end_date")
    private LocalDateTime recurrenceEndDate; // 반복 종료일

    // 알림 설정 (n일 전 알림)
    @ElementCollection
    @CollectionTable(name = "calendar_reminders", joinColumns = @JoinColumn(name = "cal_no"))
    @Column(name = "reminder_days_before")
    @Builder.Default
    private List<Integer> reminderDaysBefore = new ArrayList<>(); // [1, 2, 7] = 1일전, 2일전, 7일전

    // 투약 관련 추가 정보 (nullable, 투약 타입일 때만 사용)
    @Column(name = "medication_name")
    private String medicationName;

    @Column(name = "dosage")
    private String dosage;

    @Column(name = "frequency")
    private String frequency;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    // OCR 원본 데이터 (디버깅/검증용)
    @Column(name = "ocr_raw_data", columnDefinition = "TEXT")
    private String ocrRawData;

    // 업데이트 메서드
    public void updateSchedule(String title, LocalDateTime startDate, LocalDateTime endDate, 
                             String description, LocalDateTime alarmTime) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.alarmTime = alarmTime;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateMedicationInfo(String medicationName, String dosage, String frequency,
                                   Integer durationDays, String instructions) {
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.instructions = instructions;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRecurrence(RecurrenceType recurrenceType, Integer recurrenceInterval, 
                               LocalDateTime recurrenceEndDate) {
        this.recurrenceType = recurrenceType;
        this.recurrenceInterval = recurrenceInterval;
        this.recurrenceEndDate = recurrenceEndDate;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateReminders(List<Integer> reminderDaysBefore) {
        this.reminderDaysBefore.clear();
        if (reminderDaysBefore != null) {
            this.reminderDaysBefore.addAll(reminderDaysBefore);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSubType(CalendarSubType subType) {
        if (subType != null) {
            this.subType = subType;
            this.updatedAt = LocalDateTime.now();
        }
    }
}
