package site.petful.campaignservice.dto.campaign;

import lombok.Getter;
import lombok.Setter;
import site.petful.campaignservice.dto.PetResponse;
import site.petful.campaignservice.entity.ApplicantStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ApplicantsResponse {

    private List<ApplicantDetail> applicants;

    public static ApplicantsResponse from(List<ApplicantResponse> applicantResponses) {
        ApplicantsResponse res = new ApplicantsResponse();
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
