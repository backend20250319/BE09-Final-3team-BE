package site.petful.campaignservice.entity.advertisement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="keyword")
@Getter
@Setter
public class Keyword {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long keywordNo;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_no", nullable = false)
    private Advertisement advertisement;
}
