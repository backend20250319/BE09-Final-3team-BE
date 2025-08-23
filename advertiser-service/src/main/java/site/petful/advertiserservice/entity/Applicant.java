package site.petful.advertiserservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import site.petful.advertiserservice.entity.advertisement.Advertisement;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name="applicant")
@Getter
@Setter
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicantNo;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private ApplicantStatus status;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_no", nullable = false)
    private Advertisement advertisement;

    // @OneToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "pet_no")
    // private Pet pet;

    private Long petNo;
}
