package site.petful.campaignservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name="review")
@Getter
@Setter
public class Review {

    @Id
    private Long applicantNo;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "applicant_no")
    private Applicant applicant;

    private String reviewUrl;

    @Enumerated(EnumType.STRING)
    private ReviewStatus isApproved;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
