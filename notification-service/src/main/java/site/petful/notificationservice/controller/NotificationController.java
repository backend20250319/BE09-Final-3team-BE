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
import site.petful.notificationservice.dto.NotificationCountDto;
import site.petful.notificationservice.dto.NotificationListResponseDto;
import site.petful.notificationservice.dto.NotificationResponseDto;
import site.petful.notificationservice.dto.NotificationCountDto;
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
        
        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [NotificationController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (NotificationListResponseDto) null));
        }

        // 사용자 ID 유효성 검증
        if (userNo <= 0) {
            log.warn("⚠️ [NotificationController] 유효하지 않은 사용자 ID: {}", userNo);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_USER_ID, (NotificationListResponseDto) null));
        }

        // 페이지 파라미터 유효성 검증
        if (pageable.getPageNumber() < 0) {
            log.warn("⚠️ [NotificationController] 유효하지 않은 페이지 번호: {}", pageable.getPageNumber());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (NotificationListResponseDto) null));
        }

        if (pageable.getPageSize() <= 0 || pageable.getPageSize() > 100) {
            log.warn("⚠️ [NotificationController] 유효하지 않은 페이지 크기: {}", pageable.getPageSize());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (NotificationListResponseDto) null));
        }
        
        try {
            Page<Notification> notifications = notificationService.getUserNotifications(userNo, pageable);
            NotificationListResponseDto response = NotificationListResponseDto.from(notifications);
            log.info("✅ [NotificationController] 사용자 알림 조회 성공: userId={}, totalElements={}",
                    userNo, notifications.getTotalElements());
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (IllegalArgumentException e) {
            log.error("❌ [NotificationController] 잘못된 파라미터: userId={}, error={}", userNo, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (NotificationListResponseDto) null));
        } catch (RuntimeException e) {
            log.error("❌ [NotificationController] 사용자 알림 조회 실패: userId={}, error={}", userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.OPERATION_FAILED, (NotificationListResponseDto) null));
        } catch (Exception e) {
            log.error("❌ [NotificationController] 예상치 못한 오류: userId={}, error={}", userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (NotificationListResponseDto) null));
        }
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
        
        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [NotificationController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (Void) null));
        }

        // 파라미터 유효성 검증
        if (notificationId == null || notificationId <= 0) {
            log.warn("⚠️ [NotificationController] 유효하지 않은 알림 ID: {}", notificationId);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_NOTIFICATION_ID, (Void) null));
        }

        if (userNo <= 0) {
            log.warn("⚠️ [NotificationController] 유효하지 않은 사용자 ID: {}", userNo);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_USER_ID, (Void) null));
        }

        try {
            notificationService.hideNotification(notificationId, userNo);
            log.info("✅ [NotificationController] 알림 숨김 처리 성공: notificationId={}, userId={}",
                    notificationId, userNo);
            return ResponseEntity.ok(ApiResponseGenerator.success());
        } catch (IllegalArgumentException e) {
            log.error("❌ [NotificationController] 잘못된 파라미터: notificationId={}, userId={}, error={}",
                    notificationId, userNo, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (Void) null));
        } catch (RuntimeException e) {
            log.error("❌ [NotificationController] 알림 숨김 처리 실패: notificationId={}, userId={}, error={}",
                    notificationId, userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.NOTIFICATION_HIDE_FAILED, (Void) null));
        } catch (Exception e) {
            log.error("❌ [NotificationController] 예상치 못한 오류: notificationId={}, userId={}, error={}",
                    notificationId, userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (Void) null));
        }
    }



    /**
     * 읽지 않은 알림 개수 조회
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<NotificationCountDto>> getUnreadNotificationCount(
            @AuthenticationPrincipal Long userNo) {
        
        log.info("🔢 [NotificationController] 읽지 않은 알림 개수 조회: userId={}", userNo);

        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [NotificationController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (NotificationCountDto) null));
        }

        // 사용자 ID 유효성 검증
        if (userNo <= 0) {
            log.warn("⚠️ [NotificationController] 유효하지 않은 사용자 ID: {}", userNo);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_USER_ID, (NotificationCountDto) null));
        }

        try {
            long unreadCount = notificationService.getUnreadNotificationCount(userNo);
            NotificationCountDto response = NotificationCountDto.of(unreadCount);
            log.info("✅ [NotificationController] 읽지 않은 알림 개수 조회 성공: userId={}, count={}", userNo, unreadCount);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (IllegalArgumentException e) {
            log.error("❌ [NotificationController] 잘못된 파라미터: userId={}, error={}", userNo, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_USER_ID, (NotificationCountDto) null));
        } catch (RuntimeException e) {
            log.error("❌ [NotificationController] 알림 개수 조회 실패: userId={}, error={}", userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.NOTIFICATION_COUNT_FAILED, (NotificationCountDto) null));
        } catch (Exception e) {
            log.error("❌ [NotificationController] 예상치 못한 오류: userId={}, error={}", userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (NotificationCountDto) null));
        }
    }

    /**
     * 알림 읽음 처리
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markNotificationAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Long userNo) {

        log.info("👁️ [NotificationController] 알림 읽음 처리: notificationId={}, userId={}",
                notificationId, userNo);

        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [NotificationController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (Void) null));
        }

        // 파라미터 유효성 검증
        if (notificationId == null || notificationId <= 0) {
            log.warn("⚠️ [NotificationController] 유효하지 않은 알림 ID: {}", notificationId);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_NOTIFICATION_ID, (Void) null));
        }

        if (userNo <= 0) {
            log.warn("⚠️ [NotificationController] 유효하지 않은 사용자 ID: {}", userNo);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_USER_ID, (Void) null));
        }

        try {
            notificationService.markNotificationAsRead(notificationId, userNo);
            log.info("✅ [NotificationController] 알림 읽음 처리 성공: notificationId={}, userId={}",
                    notificationId, userNo);
            return ResponseEntity.ok(ApiResponseGenerator.success());
        } catch (IllegalArgumentException e) {
            log.error("❌ [NotificationController] 잘못된 파라미터: notificationId={}, userId={}, error={}",
                    notificationId, userNo, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (Void) null));
        } catch (RuntimeException e) {
            log.error("❌ [NotificationController] 알림 읽음 처리 실패: notificationId={}, userId={}, error={}",
                    notificationId, userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.NOTIFICATION_READ_FAILED, (Void) null));
        } catch (Exception e) {
            log.error("❌ [NotificationController] 예상치 못한 오류: notificationId={}, userId={}, error={}",
                    notificationId, userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (Void) null));
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllNotificationsAsRead(
            @AuthenticationPrincipal Long userNo) {

        log.info("👁️ [NotificationController] 모든 알림 읽음 처리: userId={}", userNo);

        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [NotificationController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (Void) null));
        }

        // 사용자 ID 유효성 검증
        if (userNo <= 0) {
            log.warn("⚠️ [NotificationController] 유효하지 않은 사용자 ID: {}", userNo);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_USER_ID, (Void) null));
        }

        try {
            notificationService.markAllNotificationsAsRead(userNo);
            log.info("✅ [NotificationController] 모든 알림 읽음 처리 성공: userId={}", userNo);
            return ResponseEntity.ok(ApiResponseGenerator.success());
        } catch (IllegalArgumentException e) {
            log.error("❌ [NotificationController] 잘못된 파라미터: userId={}, error={}", userNo, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_USER_ID, (Void) null));
        } catch (RuntimeException e) {
            log.error("❌ [NotificationController] 모든 알림 읽음 처리 실패: userId={}, error={}", userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.NOTIFICATION_READ_FAILED, (Void) null));
        } catch (Exception e) {
            log.error("❌ [NotificationController] 예상치 못한 오류: userId={}, error={}", userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (Void) null));
        }
    }

}
