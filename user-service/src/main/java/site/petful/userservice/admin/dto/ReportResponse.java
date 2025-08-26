package site.petful.userservice.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import site.petful.userservice.admin.entity.ReportLog;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReportResponse {
    private Long reportId;
    private String reason;
    private Long reporterNo;
    private Long targetNo;
    private LocalDateTime createdAt;

    public static ReportResponse from(ReportLog r){
       return new ReportResponse(
               r.getId(),
               r.getReason(),
               r.getReporterNo(),
               r.getTargetNo(),
               r.getCreatedAt()
       );
    }
}
