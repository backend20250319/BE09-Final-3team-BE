package site.petful.advertiserservice.entity;

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
@Table(name = "advertiser")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Advertiser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String businessNumber;

    @Column
    private String phoneNumber;

    @Column
    private String address;

    @Column
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdvertiserStatus status = AdvertiserStatus.PENDING;

    @Column
    private String verificationCode;

    @Column
    private LocalDateTime verificationCodeExpiry;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum AdvertiserStatus {
        PENDING,    // 승인 대기
        APPROVED,   // 승인됨
        REJECTED,   // 거부됨
        SUSPENDED   // 정지됨
    }
}