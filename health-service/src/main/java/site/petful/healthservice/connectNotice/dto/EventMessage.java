package site.petful.healthservice.connectNotice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage {

    private String eventId;
    private String type;
    private Instant occurredAt;
    private Actor actor;
    private Target target;
    private Map<String, Object> attributes;
    private Integer schemaVersion;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Actor {
        private Long id;
        private String name;
    }

    public enum ResourceType { POST, COMMENT, CAMPAIGN, USER, ETC }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Target {
        private String userId;
        private Long resourceId;
        private String resourceType;
    }
}
