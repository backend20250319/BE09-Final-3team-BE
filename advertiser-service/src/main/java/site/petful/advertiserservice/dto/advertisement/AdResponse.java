package site.petful.advertiserservice.dto.advertisement;

import lombok.*;
import site.petful.advertiserservice.dto.advertiser.AdvertiserResponse;
import site.petful.advertiserservice.entity.advertisement.AdStatus;
import site.petful.advertiserservice.entity.advertisement.Advertisement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private String reason;
    private AdvertiserResponse advertiser;

    private List<MissionResponse> mission;
    private List<KeywordResponse> keyword;
    private List<RequirementResponse> requirement;

    private Boolean isDeleted;

    public static AdResponse from(Advertisement ad) {
        AdResponse res = new AdResponse();
        res.setAdNo(ad.getAdNo());
        res.setTitle(ad.getTitle());
        res.setContent(ad.getContent());
        res.setObjective(ad.getObjective());
        res.setAnnounceStart(ad.getAnnounceStart());
        res.setAnnounceEnd(ad.getAnnounceEnd());
        res.setCampaignSelect(ad.getCampaignSelect());
        res.setCampaignStart(ad.getCampaignStart());
        res.setCampaignEnd(ad.getCampaignEnd());
        res.setApplicants(ad.getApplicants());
        res.setMembers(ad.getMembers());
        res.setAdStatus(ad.getAdStatus());
        res.setAdUrl(ad.getAdUrl());
        res.setCreatedAt(ad.getCreatedAt());
        res.setReason(ad.getReason());
        res.setIsDeleted(ad.getIsDeleted());

        if (ad.getMission() != null) {
            res.mission = ad.getMission().stream()
                    .map(MissionResponse::from)
                    .collect(Collectors.toList());
        }

        if (ad.getKeyword() != null) {
            res.keyword = ad.getKeyword().stream()
                    .map(KeywordResponse::from)
                    .collect(Collectors.toList());
        }

        if (ad.getRequirement() != null) {
            res.requirement = ad.getRequirement().stream()
                    .map(RequirementResponse::from)
                    .collect(Collectors.toList());
        }

        res.advertiser = AdvertiserResponse.from(ad.getAdvertiser());

        return res;
    }
}
