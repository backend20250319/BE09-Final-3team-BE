package site.petful.userservice.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="Report_Log")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReportLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="report_no")
    private Long id;

    @Column(name="reason", nullable=false)
    private String reason;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="type",column = @Column(name="reporter_type", nullable=false,length = 20)),
            @AttributeOverride(name="id", column = @Column (name="reporter_no",nullable=false))
    })
    private ActorRef reporter;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="type", column=@Column(name="target_type", nullable=false, length=20)),
            @AttributeOverride(name="id",   column=@Column(name="target_no",   nullable=false))
    })
    private ActorRef target;

    @Column(name="post_no",nullable = true)
    private Long postNo;

    @Column(name="comment_no",nullable = true)
    private Long commentNo;

    @Column(name="created_at",nullable = false)
    private LocalDateTime createdAt;

    @Column(name="report_status",nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportStatus reportStatus = ReportStatus.BEFORE;

    @Column(name="reject_reason", nullable = true)
    private String rejectReason;

    @PrePersist
    protected void onCreate() {if(createdAt == null) createdAt = LocalDateTime.now();}

    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public void setReporter(ActorRef reporter) {
        this.reporter = reporter;
    }
    
    public void setTarget(ActorRef target) {
        this.target = target;
    }

    public void setReportStatus(ReportStatus reportStatus) {
        this.reportStatus = reportStatus;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
