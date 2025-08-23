package site.petful.advertiserservice.dto.campaign;

import lombok.Getter;
import lombok.Setter;
import site.petful.advertiserservice.dto.advertisement.AdResponse;
import site.petful.advertiserservice.entity.Applicant;
import site.petful.advertiserservice.entity.ApplicantStatus;

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
