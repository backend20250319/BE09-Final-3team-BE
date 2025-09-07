package site.petful.advertiserservice.entity.advertisement;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import site.petful.advertiserservice.entity.advertiser.Advertiser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name="advertisement")
@Getter
@Setter
public class Advertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adNo;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private String objective;

    @Column(nullable = false)
    private LocalDate announceStart;

    @Column(nullable = false)
    private LocalDate announceEnd;

    @Column(nullable = false)
    private LocalDate campaignSelect;

    @Column(nullable = false)
    private LocalDate campaignStart;

    @Column(nullable = false)
    private LocalDate campaignEnd;

    @Column(nullable = false)
    private Integer applicants;

    @Column(nullable = false)
    private Integer members;

    @Enumerated(EnumType.STRING)
    private AdStatus adStatus;

    @Column(nullable = false)
    private String adUrl;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertiser_no")
    private Advertiser advertiser;

    @OneToMany(mappedBy = "advertisement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mission> mission = new ArrayList<>();

    @OneToMany(mappedBy = "advertisement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Keyword> keyword = new ArrayList<>();

    @OneToMany(mappedBy = "advertisement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Requirement> requirement = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isDeleted;

}
