package site.petful.campaignservice.dto.campaign;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import site.petful.campaignservice.dto.pet.PetResponse;
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
                        applicant.getApplicantNo(),
                        applicant.getPet(),
                        applicant.getContent(),
                        applicant.getStatus(),
                        applicant.getIsSaved(),
                        applicant.getCreatedAt(),
                        applicant.getIsDeleted()))
                .collect(Collectors.toList());
        return res;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ApplicantDetail {
        private Long applicantNo;
        private PetResponse pet;
        private String content;
        private ApplicantStatus status;
        private Boolean isSaved;
        private LocalDateTime createdAt;
        private Boolean isDeleted;
    }
}
