package site.petful.notificationservice.controller;


import site.petful.notificationservice.entity.Notification;
import site.petful.notificationservice.service.NotificationReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationReadService notificationService;

    @GetMapping
    public Slice<Notification> list(
    @RequestParam  Long userId,
    @RequestParam(defaultValue = "0")int page,
    @RequestParam(defaultValue = "5")int size){
        return notificationService.listVisible(userId, page, size);
    }

    @PatchMapping("{id}/hide")
    public ResponseEntity<Void> hide(
            @PathVariable Long id,
            @RequestParam Long userId
    ){
        notificationService.hide(id, userId);
        return ResponseEntity.noContent().build();
    }
}
