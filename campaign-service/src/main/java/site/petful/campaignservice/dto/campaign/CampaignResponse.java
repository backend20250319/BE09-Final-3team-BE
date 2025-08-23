package site.petful.campaignservice.dto.campaign;

import lombok.Getter;
import lombok.Setter;
import site.petful.campaignservice.entity.Applicant;
import site.petful.campaignservice.entity.ApplicantStatus;
import site.petful.campaignservice.entity.advertisement.Advertisement;
import site.petful.campaignservice.entity.Pet;

import java.time.LocalDateTime;

@Getter
@Setter
public class CampaignResponse {

    private Advertisement advertisement;
    private Pet pet;
    private String content;
    private ApplicantStatus status;
    private LocalDateTime createdAt;

    public static CampaignResponse from(Applicant applicant) {
        CampaignResponse res = new CampaignResponse();
        res.setAdvertisement(applicant.getAdvertisement());
        res.setPet(applicant.getPet());
        res.setContent(applicant.getContent());
        res.setStatus(applicant.getStatus());
        res.setCreatedAt(applicant.getCreatedAt());

        return res;
    }
}
