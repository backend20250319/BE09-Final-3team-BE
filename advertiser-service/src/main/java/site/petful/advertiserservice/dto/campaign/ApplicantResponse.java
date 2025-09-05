package site.petful.advertiserservice.dto.campaign;

import lombok.Getter;
import lombok.Setter;
import site.petful.advertiserservice.dto.advertisement.AdResponse;
import site.petful.advertiserservice.entity.ApplicantStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApplicantResponse {

    private Long applicantNo;
    private Long adNo;
    private PetResponse pet;
    private String content;
    private ApplicantStatus status;
    private Boolean isSaved;
    private LocalDateTime createdAt;
}
