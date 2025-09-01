package site.petful.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.petful.notificationservice.dto.EventMessage;
import site.petful.notificationservice.entity.Notification;
import site.petful.notificationservice.service.NotificationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 사용자별 알림 목록 조회
     */
    @GetMapping("/users/")
    public ResponseEntity<Page<Notification>> getUserNotifications(
           @AuthenticationPrincipal Long userNo,
            Pageable pageable) {
        
        log.info("📋 [NotificationController] 사용자 알림 조회: userId={}, page={}, size={}",
                userNo, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Notification> notifications = notificationService.getUserNotifications(userNo, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 알림 상세 조회
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<Notification> getNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Long userNo) {
        
        log.info("📋 [NotificationController] 알림 상세 조회: notificationId={}, userId={}", 
                notificationId, userNo);
        
        Notification notification = notificationService.getNotification(notificationId, userNo);
        return ResponseEntity.ok(notification);
    }

    /**
     * 알림 숨김 처리
     */
    @PatchMapping("/{notificationId}/hide")
    public ResponseEntity<Map<String, Object>> hideNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Long userNo) {
        
        log.info("🙈 [NotificationController] 알림 숨김: notificationId={}, userId={}", 
                notificationId, userNo);
        
        notificationService.hideNotification(notificationId, userNo);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "알림이 숨겨졌습니다.");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 즉시 알림 테스트
     */
    @PostMapping("/test/immediate")
    public ResponseEntity<Map<String, Object>> testImmediateNotification(@RequestBody EventMessage eventMessage) {
        log.info("🧪 [NotificationController] 즉시 알림 테스트: eventId={}, type={}", 
                eventMessage.getEventId(), eventMessage.getType());

        try {
            Notification notification = notificationService.createImmediateNotification(eventMessage);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "즉시 알림 생성 및 발송 완료");
            response.put("notificationId", notification.getId());
            response.put("status", notification.getStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ [NotificationController] 즉시 알림 테스트 실패: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즉시 알림 테스트 실패: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 예약 알림 테스트
     */
    @PostMapping("/test/scheduled")
    public ResponseEntity<Map<String, Object>> testScheduledNotification(
            @RequestBody EventMessage eventMessage,
            @RequestParam int delayMinutes) {
        
        log.info("🧪 [NotificationController] 예약 알림 테스트: eventId={}, type={}, delayMinutes={}", 
                eventMessage.getEventId(), eventMessage.getType(), delayMinutes);

        try {
            Notification notification = notificationService.createScheduledNotification(eventMessage, delayMinutes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "예약 알림 생성 완료");
            response.put("notificationId", notification.getId());
            response.put("scheduledAt", notification.getScheduledAt());
            response.put("status", notification.getStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ [NotificationController] 예약 알림 테스트 실패: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "예약 알림 테스트 실패: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 알림 상태 조회
     */
    @GetMapping("/test/{notificationId}/status")
    public ResponseEntity<Map<String, Object>> getNotificationStatus(@PathVariable Long notificationId) {
        log.info("🧪 [NotificationController] 알림 상태 조회: notificationId={}", notificationId);

        try {
            Notification notification = notificationService.getNotificationById(notificationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "알림 상태 조회 완료");
            response.put("notificationId", notification.getId());
            response.put("status", notification.getStatus());
            response.put("createdAt", notification.getCreatedAt());
            response.put("scheduledAt", notification.getScheduledAt());
            response.put("sentAt", notification.getSentAt());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ [NotificationController] 알림 상태 조회 실패: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "알림 상태 조회 실패: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
