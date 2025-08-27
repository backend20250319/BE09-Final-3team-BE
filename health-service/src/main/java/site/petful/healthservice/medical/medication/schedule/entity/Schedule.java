package site.petful.healthservice.medical.medication.schedule.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.healthservice.medical.medication.schedule.enums.ScheduleMainType;
import site.petful.healthservice.medical.medication.schedule.enums.ScheduleSubType;
import site.petful.healthservice.medical.medication.schedule.enums.RecurrenceType;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "schedule")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_no")
    private Long scheduleNo;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "main_type", nullable = false)
    private ScheduleMainType mainType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_type", nullable = false)
    private ScheduleSubType subType;

    @Column(name = "all_day", nullable = false)
    @Builder.Default
    private Boolean allDay = false;

    @Column(name = "alarm_time")
    private LocalDateTime alarmTime;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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

    // 알림 설정 (n일 전 알림) - 단일 값으로 변경
    @Column(name = "reminder_days_before")
    private Integer reminderDaysBefore; // 1 = 1일전, 2 = 2일전, 7 = 7일전

    @Column(name = "frequency")
    private String frequency;

    // 스케줄 시간 (예: "08:00,20:00") 
    @Column(name = "times", columnDefinition = "TEXT")
    private String times;

    // 업데이트 메서드
    public void updateSchedule(String title, LocalDateTime startDate, LocalDateTime endDate,
                             LocalDateTime alarmTime) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.alarmTime = alarmTime;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateFrequency(String frequency) {
        this.frequency = frequency;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRecurrence(RecurrenceType recurrenceType, Integer recurrenceInterval, 
                               LocalDateTime recurrenceEndDate) {
        this.recurrenceType = recurrenceType;
        this.recurrenceInterval = recurrenceInterval;
        this.recurrenceEndDate = recurrenceEndDate;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateReminders(Integer reminderDaysBefore) {
        this.reminderDaysBefore = reminderDaysBefore;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSubType(ScheduleSubType subType) {
        if (subType != null) {
            this.subType = subType;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void updateTimes(List<LocalTime> times) {
        if (times != null && !times.isEmpty()) {
            this.times = times.stream()
                .map(LocalTime::toString)
                .collect(Collectors.joining(","));
        } else {
            this.times = null;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void ensureDeletedFlag() {
        if (this.deleted == null) {
            this.deleted = false;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    // times 문자열을 List<LocalTime>으로 변환
    public List<LocalTime> getTimesAsList() {
        if (this.times == null || this.times.isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(this.times.split(","))
            .stream()
            .map(String::trim)
            .map(LocalTime::parse)
            .collect(Collectors.toList());
    }
}
