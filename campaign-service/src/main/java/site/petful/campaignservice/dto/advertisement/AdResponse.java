package site.petful.campaignservice.dto.advertisement;

import lombok.Getter;
import lombok.Setter;
import site.petful.campaignservice.entity.AdStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AdResponse {

    private Long adNo;
    private String title;
    private String content;
    private String objective;
    private LocalDate announceStart;
    private LocalDate announceEnd;
    private LocalDate campaignSelect;
    private LocalDate campaignStart;
    private LocalDate campaignEnd;
    private Integer applicants;
    private Integer members;
    private AdStatus adStatus;
    private String adUrl;
    private LocalDateTime createdAt;
    private AdvertiserResponse advertiser;

    private List<MissionResponse> mission;
    private List<KeywordResponse> keyword;
    private List<RequirementResponse> requirement;

    private Boolean isDeleted;
}