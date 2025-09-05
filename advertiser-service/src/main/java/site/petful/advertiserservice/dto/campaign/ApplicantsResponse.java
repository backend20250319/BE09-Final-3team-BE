package site.petful.advertiserservice.dto.campaign;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import site.petful.advertiserservice.dto.advertisement.AdResponse;
import site.petful.advertiserservice.entity.ApplicantStatus;
import site.petful.advertiserservice.entity.advertisement.Advertisement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ApplicantsResponse {

    private List<ApplicantDetail> applicants;

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
    }
}
