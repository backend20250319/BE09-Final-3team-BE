package site.petful.mypageservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_email", unique = true, nullable = false)
    private String email; // user-service의 email과 연결
    
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    
    @Column(name = "self_introduction", columnDefinition = "TEXT")
    private String selfIntroduction;
    
    @Column(name = "instagram_username")
    private String instagramUsername;
    
    @Column(name = "instagram_connected")
    private Boolean instagramConnected = false;
    
    @Column(name = "preferred_pet_type")
    private String preferredPetType; // 강아지, 고양이, 새, 기타
    
    @Column(name = "pet_count")
    private Integer petCount = 0;
    
    @Column(name = "is_influencer")
    private Boolean isInfluencer = false;
    
    @Column(name = "influencer_category")
    private String influencerCategory; // 반려동물, 리뷰, 일상 등
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
