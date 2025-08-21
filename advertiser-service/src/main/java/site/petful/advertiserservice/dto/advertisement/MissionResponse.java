package site.petful.advertiserservice.dto.advertisement;

import lombok.Getter;
import lombok.Setter;
import site.petful.advertiserservice.entity.advertisement.Mission;

@Getter
@Setter
public class MissionResponse {
    private Long missionNo;
    private String content;

    public static MissionResponse from(Mission mission) {
        MissionResponse res = new MissionResponse();
        res.setMissionNo(mission.getMissionNo());
        res.setContent(mission.getContent());
        return res;
    }
}