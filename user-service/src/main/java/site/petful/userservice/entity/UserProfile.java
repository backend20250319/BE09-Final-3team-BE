package site.petful.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_no")
    private Long profileNo;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no", nullable = false, unique = true)
    private User user;
    
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;
    
    @Column(name = "self_introduction", columnDefinition = "TEXT")
    private String selfIntroduction;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Column(name = "road_address", length = 255)
    private String roadAddress;
    
    @Column(name = "detail_address", length = 255)
    private String detailAddress;
    
    @Column(name = "instagram_account", length = 100)
    private String instagramAccount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

