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
import java.time.LocalTime;
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

    // 알림 설정 (n일 전 알림)
    @ElementCollection
    @CollectionTable(name = "calendar_reminders", joinColumns = @JoinColumn(name = "cal_no"))
    @Column(name = "reminder_days_before")
    @Builder.Default
    private List<Integer> reminderDaysBefore = new ArrayList<>(); // [1, 2, 7] = 1일전, 2일전, 7일전

    @Column(name = "frequency")
    private String frequency;

    // 투약 시간들 (예: ["08:00", "20:00"])
    @ElementCollection
    @CollectionTable(name = "calendar_times", joinColumns = @JoinColumn(name = "cal_no"))
    @Column(name = "time")
    @Builder.Default
    private List<LocalTime> times = new ArrayList<>();

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

    public void updateReminders(List<Integer> reminderDaysBefore) {
        // 새로운 ArrayList를 할당하여 불변 컬렉션 문제 해결
        this.reminderDaysBefore = new ArrayList<>();
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

    public void updateTimes(List<LocalTime> times) {
        this.times = new ArrayList<>();
        if (times != null) {
            this.times.addAll(times);
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
}
