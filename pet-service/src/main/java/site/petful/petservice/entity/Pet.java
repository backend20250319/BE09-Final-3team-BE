package site.petful.petservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "Pet")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_no")
    private Long petNo;

    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "type", length = 255, nullable = false)
    private String type;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "age", nullable = false)
    private Long age;

    @Column(name = "gender", length = 1, nullable = false)
    private String gender;

    @Column(name = "weight")
    private Float weight;

    @Column(name = "is_petstar", nullable = false)
    private Boolean isPetStar = false;

    @Column(name = "user_no", nullable = false)
    private Long userNo;

    @Column(name = "sns_profile_no", nullable = true)
    private Long snsProfileNo;

    @Column(name = "petstar_status")
    @Enumerated(EnumType.STRING)
    private PetStarStatus petStarStatus = PetStarStatus.NONE;

    @Column(name = "pending_at")
    private LocalDateTime pendingAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "image_no")
    private Long imageNo;
}
