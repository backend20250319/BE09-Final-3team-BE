package site.petful.notificationservice.dto;

import java.time.Instant;
import java.util.Map;

public record EventMessage (
    String eventId,
    String type,
    Instant occuerdAt,
    Actor actor,
    Target target,
    Map<String, Object> attributes
    ){
    public record Actor (Long id, String name) {}
    public record Target (String userId, Long resourceId,String resourceType) {}
}
