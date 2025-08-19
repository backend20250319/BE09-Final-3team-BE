package site.petful.notificationservice.controller;
import site.petful.notificationservice.dto.EventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/__ops/rabbit") // 운영 배포 전 제거 권장
@RequiredArgsConstructor
public class RabbitOpsController {
    private final RabbitTemplate rabbit;
    @Value("${app.messaging.exchange}") private String ex;

    @PostMapping("/publish")
    public ResponseEntity<Void> publish(@RequestParam String routingKey, @RequestBody EventMessage eventMessage) {
        rabbit.convertAndSend(ex, routingKey, eventMessage);
        return ResponseEntity.accepted().build();
    }
}
