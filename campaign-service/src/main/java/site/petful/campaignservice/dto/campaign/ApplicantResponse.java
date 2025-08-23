package site.petful.campaignservice.dto.campaign;

import lombok.Getter;
import lombok.Setter;
import site.petful.campaignservice.dto.advertisement.AdResponse;
import site.petful.campaignservice.entity.Applicant;
import site.petful.campaignservice.entity.ApplicantStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApplicantResponse {

    private AdResponse advertisement;
    private PetResponse pet;
    private String content;
    private ApplicantStatus status;
    private LocalDateTime createdAt;

    public static ApplicantResponse from(Applicant applicant, PetResponse petResponse) {
        ApplicantResponse res = new ApplicantResponse();
        res.advertisement = AdResponse.from(applicant.getAdvertisement());
        res.setPet(petResponse);
        res.setContent(applicant.getContent());
        res.setStatus(applicant.getStatus());
        res.setCreatedAt(applicant.getCreatedAt());
        return res;
    }
}
