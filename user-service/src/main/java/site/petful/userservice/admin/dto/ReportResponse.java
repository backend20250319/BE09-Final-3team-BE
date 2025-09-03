package site.petful.userservice.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.petful.userservice.admin.entity.ActorType;
import site.petful.userservice.admin.entity.ReportLog;
import site.petful.userservice.admin.entity.ReportStatus;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponse {
    private Long reportId;
    private ActorType reporterType;
    private Long reporterId;
    private String reporterNickname;
    private String reporterProfileImage;
    private ActorType targetType;
    private Long targetId;
    private String targetNickname;
    private String targetProfileImage;
    private String reason;
    private ReportStatus status;
    private LocalDateTime createdAt;
}
