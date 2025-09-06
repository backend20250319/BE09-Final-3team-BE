package site.petful.advertiserservice.dto.campaign;

import lombok.Getter;
import lombok.Setter;
import site.petful.advertiserservice.entity.ApplicantStatus;

@Getter
@Setter
public class ApplicantRequest {

    private String content;
    private ApplicantStatus status;
    private Boolean isSaved;
}
