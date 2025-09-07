package site.petful.advertiserservice.entity.advertiser;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name="advertiser_files")
@Getter
@Setter
public class AdvertiserFiles {

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

    @Enumerated(EnumType.STRING)
    private FileType type;

    @ManyToOne
    @JoinColumn(name = "advertiser_no")
    private Advertiser advertiser;
}
