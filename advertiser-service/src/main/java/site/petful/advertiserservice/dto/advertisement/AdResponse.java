package site.petful.advertiserservice.dto.advertisement;

import lombok.*;
import site.petful.advertiserservice.dto.advertiser.AdvertiserResponse;
import site.petful.advertiserservice.entity.advertisement.AdStatus;
import site.petful.advertiserservice.entity.advertisement.Advertisement;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class AdResponse {

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

    public static AdResponse from(Advertisement ad) {
        AdResponse res = new AdResponse();
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
        res.advertiser = AdvertiserResponse.from(ad.getAdvertiser());

        return res;
    }
}
