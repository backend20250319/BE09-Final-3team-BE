package site.petful.advertiserservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ReportRequest {

    @NotBlank(message = "신고 사유는 필수입니다.")
    @Size(min = 10, max = 500, message = "신고 사유는 10자 이상 500자 이하로 입력해주세요.")
    private String reason;

    @NotBlank(message = "신고 대상 이름은 필수입니다.")
    private String targetName;
}
