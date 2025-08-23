package site.petful.campaignservice.dto.advertisement;

import lombok.Getter;
import lombok.Setter;
import site.petful.campaignservice.entity.advertisement.Requirement;

@Getter
@Setter
public class RequirementResponse {
    private Long reqNo;
    private String content;

    public static RequirementResponse from(Requirement requirement) {
        RequirementResponse res = new RequirementResponse();
        res.setReqNo(requirement.getReqNo());
        res.setContent(requirement.getContent());
        return res;
    }
}
