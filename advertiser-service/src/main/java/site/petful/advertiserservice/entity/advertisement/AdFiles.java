package site.petful.advertiserservice.entity.advertisement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name="ad_files")
@Getter
@Setter
public class AdFiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileNo;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String savedName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    @OneToOne
    @JoinColumn(name = "ad_no")
    private Advertisement advertisement;
}
