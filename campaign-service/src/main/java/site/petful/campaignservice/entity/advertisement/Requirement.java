package site.petful.campaignservice.entity.advertisement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="requirement")
@Getter
@Setter
public class Requirement {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long reqNo;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_no", nullable = false)
    private Advertisement advertisement;
}
