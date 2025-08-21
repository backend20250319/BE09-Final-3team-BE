package site.petful.advertiserservice.dto.advertisement;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class AdRequest {

    private String title;
    private String content;
    private String objective;
    private LocalDate announceStart;
    private LocalDate announceEnd;
    private LocalDate campaignSelect;
    private LocalDate campaignStart;
    private LocalDate campaignEnd;
    private Integer members;
    private String adUrl;

    private List<MissionRequest> mission;
    private List<KeywordRequest> keyword;
    private List<RequirementRequest> requirement;

}
