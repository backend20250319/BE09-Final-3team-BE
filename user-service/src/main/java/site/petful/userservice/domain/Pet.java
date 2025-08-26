package site.petful.userservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="Pet")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="pet_no")
    private Long petNo;

    @Column(name="name", length = 30,nullable = false)
    private String name;

    @Column(name="type", length = 255,nullable = false)
    private String type;

    @Column(name="age", nullable = false)
    private Integer age;

    @Column(name="gender", length = 1, nullable = false)
    private String gender;

    @Column(name="petstar_status")
    @Enumerated(EnumType.STRING)
    private PetStarStatus petStarStatus  = PetStarStatus.NONE;

    @Column(name="user_no", nullable = false)
    private Long userNo;

    @Column(name="image_no", nullable = false)
    private Long imageNo;

    @Column(name="sns_profile_no", nullable = false)
    private Long snsProfileNo;

    @Column(name ="pending_at",nullable = true)
    private LocalDateTime pendingAt;

    public void setPetStarStatus(PetStarStatus petStarStatus) {
    }
}
