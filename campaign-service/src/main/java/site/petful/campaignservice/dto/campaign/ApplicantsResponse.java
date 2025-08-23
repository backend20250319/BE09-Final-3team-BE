package site.petful.campaignservice.dto.campaign;

import lombok.Getter;
import lombok.Setter;
import site.petful.campaignservice.dto.advertisement.AdResponse;
import site.petful.campaignservice.entity.ApplicantStatus;
import site.petful.campaignservice.entity.advertisement.Advertisement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ApplicantsResponse {

    private AdResponse advertisement;
    private List<ApplicantDetail> applicants;

    public static ApplicantsResponse from(Advertisement ad, List<ApplicantResponse> applicantResponses) {
        ApplicantsResponse res = new ApplicantsResponse();
        res.advertisement = AdResponse.from(ad);
        res.applicants = applicantResponses.stream()
                .map(applicant -> new ApplicantDetail(
                        applicant.getPet(),
                        applicant.getContent(),
                        applicant.getStatus(),
                        applicant.getCreatedAt()))
                .collect(Collectors.toList());
        return res;
    }

    @Getter
    @Setter
    public static class ApplicantDetail {
        private PetResponse pet;
        private String content;
        private ApplicantStatus status;
        private LocalDateTime createdAt;

        public ApplicantDetail(PetResponse pet, String content, ApplicantStatus status, LocalDateTime createdAt) {
            this.pet = pet;
            this.content = content;
            this.status = status;
            this.createdAt = createdAt;
        }
    }
}
