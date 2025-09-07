package site.petful.healthservice.activity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.petful.healthservice.activity.enums.ActivityLevel;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activity")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Activity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_no")
    private Long activityNo;
    
    @Column(name = "user_no", nullable = false)
    private Long userNo;
    
    @Column(name = "pet_no", nullable = false)
    private Long petNo;
    
    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;
    
    @Column(name = "walking_distance_km", nullable = false)
    private Double walkingDistanceKm;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false)
    private ActivityLevel activityLevel;
    
    @Column(name = "calories_burned", nullable = false)
    private Integer caloriesBurned; // 소모 칼로리
    
    @Column(name = "recommended_calories_burned", nullable = false)
    private Integer recommendedCaloriesBurned; // 권장 소모 칼로리
    
    @Column(name = "weight_kg", nullable = false)
    private Double weightKg;
    
    @Column(name = "sleep_hours", nullable = false)
    private Double sleepHours;
    
    @Column(name = "poop_count", nullable = false)
    private Integer poopCount;
    
    @Column(name = "pee_count", nullable = false)
    private Integer peeCount;
    
    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;
    
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ActivityMeal> meals = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 식사 추가 메서드
    public void addMeal(ActivityMeal meal) {
        meals.add(meal);
        meal.setActivity(this);
    }
    
    // 식사 제거 메서드
    public void removeMeal(ActivityMeal meal) {
        meals.remove(meal);
        meal.setActivity(null);
    }
}
