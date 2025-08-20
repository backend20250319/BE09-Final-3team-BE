package site.petful.advertiserservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="advertiser")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Advertiser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="advertiser_no")
    private Long advertiserNo;

    @Column(name="user_id", nullable=false)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    private String website;

    @Column(nullable = false)
    private String email;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name="is_active", nullable = false)
    private Boolean isActive = false;

    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved = false;

    private String reason;

    @Column(name="doc_no", nullable = false)
    private Long docNo;

    @Column(name = "profile_no")
    private Long profileNo;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
