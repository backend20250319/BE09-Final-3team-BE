package site.petful.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.petful.notificationservice.common.ApiResponse;
import site.petful.notificationservice.common.ApiResponseGenerator;
import site.petful.notificationservice.common.ErrorCode;
import site.petful.notificationservice.dto.EventMessage;
import site.petful.notificationservice.dto.NotificationListResponseDto;
import site.petful.notificationservice.dto.NotificationResponseDto;
import site.petful.notificationservice.entity.Notification;
import site.petful.notificationservice.service.NotificationService;

@Slf4j
@RestController
@RequestMapping("/notifications/noti")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 사용자별 알림 목록 조회
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<NotificationListResponseDto>> getUserNotifications(
            @AuthenticationPrincipal Long userNo,
            @PageableDefault Pageable pageable) {
        
        log.info("📋 [NotificationController] 사용자 알림 조회: userId={}, page={}, size={}",
                userNo, pageable.getPageNumber(), pageable.getPageSize());
        
        // userNo가 null인 경우 처리
        if (userNo == null) {
            log.error("❌ [NotificationController] 사용자 ID가 null입니다.");
            return ResponseEntity.badRequest().body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (NotificationListResponseDto) null));
        }
        
        try {
            Page<Notification> notifications = notificationService.getUserNotifications(userNo, pageable);
            NotificationListResponseDto response = NotificationListResponseDto.from(notifications);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (Exception e) {
            log.error("❌ [NotificationController] 사용자 알림 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponseGenerator.fail(ErrorCode.OPERATION_FAILED, (NotificationListResponseDto) null));
        }
    }

    /**
     * 알림 상세 조회
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponseDto>> getNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Long userNo) {
        
        log.info("📋 [NotificationController] 알림 상세 조회: notificationId={}, userId={}", 
                notificationId, userNo);
        
        Notification notification = notificationService.getNotification(notificationId, userNo);
        NotificationResponseDto response = NotificationResponseDto.from(notification);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }

    /**
     * 알림 숨김 처리
     */
    @PatchMapping("/{notificationId}/hide")
    public ResponseEntity<ApiResponse<Void>> hideNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Long userNo) {
        
        log.info("🙈 [NotificationController] 알림 숨김: notificationId={}, userId={}", 
                notificationId, userNo);
        
        notificationService.hideNotification(notificationId, userNo);
        
        return ResponseEntity.ok(ApiResponseGenerator.success());
    }

    /**
     * 예약 알림 테스트
     */
    @PostMapping("/test/scheduled")
    public ResponseEntity<ApiResponse<NotificationResponseDto>> testScheduledNotification(
            @RequestBody EventMessage eventMessage,
            @RequestParam String timeStr) {
        
        log.info("🧪 [NotificationController] 예약 알림 테스트: eventId={}, type={}, timeStr={}", 
                eventMessage.getEventId(), eventMessage.getType(), timeStr);

        try {
            Notification notification = notificationService.createScheduledNotification(eventMessage, timeStr);
            NotificationResponseDto response = NotificationResponseDto.from(notification);
            
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
            
        } catch (Exception e) {
            log.error("❌ [NotificationController] 예약 알림 테스트 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponseGenerator.fail(ErrorCode.OPERATION_FAILED, (NotificationResponseDto) null));
        }
    }

}
