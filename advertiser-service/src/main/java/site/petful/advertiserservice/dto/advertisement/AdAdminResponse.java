package site.petful.advertiserservice.dto.advertisement;

import lombok.*;

import site.petful.advertiserservice.entity.advertisement.AdStatus;
import site.petful.advertiserservice.entity.advertisement.Advertisement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class AdAdminResponse {

    private Long adNo;
    private String title;
    private String description;
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
    
    // 광고주 정보 (프론트엔드에서 사용)
    private String advertiserName;
    private String advertiserWebsite;
    private String advertiserLogo;

    private List<MissionResponse> mission;
    private List<KeywordResponse> keyword;
    private List<RequirementResponse> requirement;

    public static AdAdminResponse from(Advertisement ad) {
        AdAdminResponse res = new AdAdminResponse();
        res.setAdNo(ad.getAdNo());
        res.setTitle(ad.getTitle());
        // content가 100글자를 넘으면 100글자로 자르고 "..." 추가
        String content = ad.getContent();
        if (content != null && content.length() > 100) {
            res.setDescription(content.substring(0, 100) + "...");
        } else {
            res.setDescription(content);
        }
        res.setObjective(ad.getObjective());
        res.setAnnounceStart(ad.getAnnounceStart());
        res.setAnnounceEnd(ad.getAnnounceEnd());
        res.setCampaignSelect(ad.getCampaignSelect());
        res.setCampaignStart(ad.getCampaignStart());
        res.setCampaignEnd(ad.getCampaignEnd());
        res.setApplicants(ad.getApplicants());
        res.setMembers(ad.getMembers());
        res.setAdStatus(ad.getAdStatus());
        // adUrl은 서비스에서 AdFiles를 통해 설정
        res.setCreatedAt(ad.getCreatedAt());
        res.setReason(ad.getReason());

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

        // 광고주 정보 설정
        if (ad.getAdvertiser() != null) {
            res.setAdvertiserName(ad.getAdvertiser().getName());
            res.setAdvertiserWebsite(ad.getAdvertiser().getWebsite());
            // advertiserLogo는 서비스에서 AdvertiserFiles를 통해 설정
        }

        return res;
    }
}
