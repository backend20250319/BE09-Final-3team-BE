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
import java.util.List;



@Entity
@Table(name = "Portfolio")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Portfolio {

    @Id
    @Column(name = "pet_no")
    private Long petNo;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "cost")
    private Long cost;

    @Column(name = "contact", columnDefinition = "TEXT")
    private String contact;

    @Column(name = "is_saved")
    private Boolean isSaved = false;

    @Column(name = "personality", columnDefinition = "TEXT")
    private String personality;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;



    // Pet과의 관계 (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_no", insertable = false, updatable = false)
    private Pet pet;

    // History와의 관계 (1:N) - 분리된 API이므로 제거
    // @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<History> histories;
}
