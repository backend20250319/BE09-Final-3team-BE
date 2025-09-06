package site.petful.campaignservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

    @Column(nullable = false)
    private Boolean isSaved;

    @Column(nullable = false)
    private Long adNo;

    @Column(nullable = false)
    private Long petNo;

    @OneToOne(mappedBy = "applicant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Review review;

    @Column(nullable = false)
    private Boolean isDeleted;
}
