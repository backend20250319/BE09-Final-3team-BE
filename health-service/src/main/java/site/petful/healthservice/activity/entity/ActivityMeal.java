package site.petful.healthservice.activity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activity_meal")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMeal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meal_no")
    private Long mealNo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;
    

    
    @Column(name = "total_weight_g", nullable = false)
    private Double totalWeightG; // 총 무게 (g)
    
    @Column(name = "total_calories", nullable = false)
    private Integer totalCalories; // 총 칼로리
    
    @Column(name = "consumed_weight_g", nullable = false)
    private Double consumedWeightG; // 섭취한 무게 (g)
    
    @Column(name = "consumed_calories", nullable = false)
    private Integer consumedCalories; // 섭취한 칼로리
    
    @Column(name = "meal_type")
    private String mealType; // 아침, 점심, 저녁, 간식 등
    
    @Column(name = "memo")
    private String memo; // 식사 관련 메모
}
