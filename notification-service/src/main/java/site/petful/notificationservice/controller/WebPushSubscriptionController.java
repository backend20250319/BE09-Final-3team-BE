package site.petful.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.petful.notificationservice.common.ApiResponse;
import site.petful.notificationservice.common.ApiResponseGenerator;
import site.petful.notificationservice.common.ErrorCode;
import site.petful.notificationservice.dto.WebPushSubscriptionRequest;
import site.petful.notificationservice.entity.WebPushSubscription;
import site.petful.notificationservice.service.WebPushSubscriptionService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 웹푸시 구독 관리를 위한 REST API 컨트롤러
 * 
 * 프론트엔드에서 구독 정보를 등록, 조회, 관리할 수 있는 엔드포인트를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/notifications/webpush")
@RequiredArgsConstructor
public class WebPushSubscriptionController {

    private final WebPushSubscriptionService subscriptionService;
    private final site.petful.notificationservice.webpush.VapidProps vapidProps;

    /**
     * VAPID 공개키를 조회합니다.
     * 
     * @return VAPID 공개키
     */
    @GetMapping("/vapid-public-key")
    public ResponseEntity<ApiResponse<String>> getVapidPublicKey() {
        log.info("🔑 [WebPushSubscriptionController] VAPID 공개키 조회");
        
        try {
            String publicKey = vapidProps.getPublicKey();
            if (publicKey == null || publicKey.trim().isEmpty()) {
                log.error("❌ [WebPushSubscriptionController] VAPID 공개키가 설정되지 않음");
                return ResponseEntity.<ApiResponse<String>>status(500).body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, "", ""));
            }
            
            log.info("✅ [WebPushSubscriptionController] VAPID 공개키 조회 성공");
            return ResponseEntity.ok(ApiResponseGenerator.success(publicKey));
            
        } catch (Exception e) {
            log.error("❌ [WebPushSubscriptionController] VAPID 공개키 조회 실패: error={}", e.getMessage(), e);
            return ResponseEntity.<ApiResponse<String>>status(500).body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, "", ""));
        }
    }

    /**
     * 웹푸시 구독 정보를 등록합니다.
     * 
     * @param request 구독 정보 요청
     * @param userNo 인증된 사용자 ID
     * @param httpRequest HTTP 요청 (User-Agent 추출용)
     * @return 등록 결과
     */
    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<Void>> subscribe(
            @RequestBody WebPushSubscriptionRequest request,
            @AuthenticationPrincipal Long userNo,
            HttpServletRequest httpRequest) {
        
        log.info("📱 [WebPushSubscriptionController] 웹푸시 구독 등록: userId={}, endpoint={}", 
                userNo, request.getEndpoint());

        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [WebPushSubscriptionController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (Void) null));
        }

        // 요청 데이터 유효성 검증
        if (request.getEndpoint() == null || request.getEndpoint().trim().isEmpty()) {
            log.warn("⚠️ [WebPushSubscriptionController] 엔드포인트가 비어있음: userId={}", userNo);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (Void) null));
        }

        if (request.getP256dhKey() == null || request.getP256dhKey().trim().isEmpty()) {
            log.warn("⚠️ [WebPushSubscriptionController] P256DH 키가 비어있음: userId={}", userNo);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (Void) null));
        }

        if (request.getAuthKey() == null || request.getAuthKey().trim().isEmpty()) {
            log.warn("⚠️ [WebPushSubscriptionController] Auth 키가 비어있음: userId={}", userNo);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (Void) null));
        }

        try {
            // User-Agent 추출
            String userAgent = httpRequest.getHeader("User-Agent");
            if (userAgent == null) {
                userAgent = "Unknown";
            }

            // 구독 정보 저장
            subscriptionService.saveSubscription(
                    userNo,
                    request.getEndpoint(),
                    request.getP256dhKey(),
                    request.getAuthKey(),
                    userAgent
            );

            log.info("✅ [WebPushSubscriptionController] 웹푸시 구독 등록 성공: userId={}", userNo);
            return ResponseEntity.ok(ApiResponseGenerator.success());

        } catch (Exception e) {
            log.error("❌ [WebPushSubscriptionController] 웹푸시 구독 등록 실패: userId={}, error={}", 
                    userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (Void) null));
        }
    }

    /**
     * 사용자의 웹푸시 구독 정보를 조회합니다.
     * 
     * @param userNo 인증된 사용자 ID
     * @return 구독 정보 목록
     */
    @GetMapping("/subscriptions")
    public ResponseEntity<ApiResponse<List<WebPushSubscription>>> getSubscriptions(
            @AuthenticationPrincipal Long userNo) {
        
        log.info("📋 [WebPushSubscriptionController] 웹푸시 구독 정보 조회: userId={}", userNo);

        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [WebPushSubscriptionController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (List<WebPushSubscription>) null));
        }

        try {
            List<WebPushSubscription> subscriptions = subscriptionService.getAllSubscriptions(userNo);
            log.info("✅ [WebPushSubscriptionController] 웹푸시 구독 정보 조회 성공: userId={}, count={}", 
                    userNo, subscriptions.size());
            return ResponseEntity.ok(ApiResponseGenerator.success(subscriptions));

        } catch (Exception e) {
            log.error("❌ [WebPushSubscriptionController] 웹푸시 구독 정보 조회 실패: userId={}, error={}", 
                    userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (List<WebPushSubscription>) null));
        }
    }

    /**
     * 특정 구독을 비활성화합니다.
     * 
     * @param subscriptionId 구독 ID
     * @param userNo 인증된 사용자 ID
     * @return 비활성화 결과
     */
    @DeleteMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(
            @PathVariable Long subscriptionId,
            @AuthenticationPrincipal Long userNo) {
        
        log.info("🚫 [WebPushSubscriptionController] 웹푸시 구독 해제: userId={}, subscriptionId={}", 
                userNo, subscriptionId);

        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [WebPushSubscriptionController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (Void) null));
        }

        // 파라미터 유효성 검증
        if (subscriptionId == null || subscriptionId <= 0) {
            log.warn("⚠️ [WebPushSubscriptionController] 유효하지 않은 구독 ID: {}", subscriptionId);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (Void) null));
        }

        try {
            // 구독 해제 전 상태 확인
            long beforeCount = subscriptionService.getSubscriptionCount(userNo);
            log.info("🔍 [WebPushSubscriptionController] 구독 해제 전 상태: userId={}, subscriptionId={}, activeCount={}", 
                    userNo, subscriptionId, beforeCount);
            
            boolean success = subscriptionService.deactivateSubscription(subscriptionId, userNo);
            
            if (success) {
                // 구독 해제 후 상태 확인
                long afterCount = subscriptionService.getSubscriptionCount(userNo);
                log.info("✅ [WebPushSubscriptionController] 웹푸시 구독 해제 성공: userId={}, subscriptionId={}, beforeCount={}, afterCount={}", 
                        userNo, subscriptionId, beforeCount, afterCount);
                return ResponseEntity.ok(ApiResponseGenerator.success());
            } else {
                log.warn("⚠️ [WebPushSubscriptionController] 구독을 찾을 수 없거나 소유자가 아님: userId={}, subscriptionId={}", 
                        userNo, subscriptionId);
                return ResponseEntity.badRequest()
                        .body(ApiResponseGenerator.fail(ErrorCode.NOT_FOUND, (Void) null));
            }

        } catch (Exception e) {
            log.error("❌ [WebPushSubscriptionController] 웹푸시 구독 해제 실패: userId={}, subscriptionId={}, error={}", 
                    userNo, subscriptionId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (Void) null));
        }
    }

    /**
     * 사용자의 웹푸시 구독 상태를 조회합니다.
     * 
     * @param userNo 인증된 사용자 ID
     * @return 구독 상태 정보
     */
    @GetMapping("/subscriptions/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSubscriptionStatus(
            @AuthenticationPrincipal Long userNo) {
        
        log.info("🔍 [WebPushSubscriptionController] 웹푸시 구독 상태 조회: userId={}", userNo);
        
        if (userNo == null) {
            log.warn("⚠️ [WebPushSubscriptionController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (Map<String, Object>) null));
        }
        
        try {
            long activeCount = subscriptionService.getSubscriptionCount(userNo);
            List<WebPushSubscription> activeSubscriptions = subscriptionService.getActiveSubscriptions(userNo);
            
            Map<String, Object> status = new HashMap<>();
            status.put("activeCount", activeCount);
            status.put("subscriptions", activeSubscriptions.stream()
                    .map(sub -> {
                        Map<String, Object> subInfo = new HashMap<>();
                        subInfo.put("id", sub.getId());
                        subInfo.put("endpoint", sub.getEndpoint());
                        subInfo.put("isActive", sub.getIsActive());
                        subInfo.put("createdAt", sub.getCreatedAt());
                        return subInfo;
                    })
                    .collect(Collectors.toList()));
            
            log.info("✅ [WebPushSubscriptionController] 구독 상태 조회 완료: userId={}, activeCount={}", 
                    userNo, activeCount);
            
            return ResponseEntity.ok(ApiResponseGenerator.success(status));
            
        } catch (Exception e) {
            log.error("❌ [WebPushSubscriptionController] 구독 상태 조회 실패: userId={}, error={}", 
                    userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (Map<String, Object>) null));
        }
    }

    /**
     * 사용자의 모든 구독을 비활성화합니다.
     * 
     * @param userNo 인증된 사용자 ID
     * @return 비활성화 결과
     */
    @DeleteMapping("/subscriptions/all")
    public ResponseEntity<ApiResponse<Void>> unsubscribeAll(
            @AuthenticationPrincipal Long userNo) {
        
        log.info("🚫 [WebPushSubscriptionController] 모든 웹푸시 구독 해제: userId={}", userNo);

        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [WebPushSubscriptionController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (Void) null));
        }

        try {
            // 구독 해제 전 상태 확인
            long beforeCount = subscriptionService.getSubscriptionCount(userNo);
            log.info("🔍 [WebPushSubscriptionController] 모든 구독 해제 전 상태: userId={}, activeCount={}", 
                    userNo, beforeCount);
            
            // 모든 활성화된 구독 조회
            List<WebPushSubscription> activeSubscriptions = subscriptionService.getActiveSubscriptions(userNo);
            
            // 모든 구독 비활성화
            int deactivatedCount = 0;
            for (WebPushSubscription subscription : activeSubscriptions) {
                boolean success = subscriptionService.deactivateSubscription(subscription.getId(), userNo);
                if (success) {
                    deactivatedCount++;
                }
            }
            
            // 구독 해제 후 상태 확인
            long afterCount = subscriptionService.getSubscriptionCount(userNo);
            log.info("✅ [WebPushSubscriptionController] 모든 웹푸시 구독 해제 완료: userId={}, deactivatedCount={}, beforeCount={}, afterCount={}", 
                    userNo, deactivatedCount, beforeCount, afterCount);
            
            return ResponseEntity.ok(ApiResponseGenerator.success());

        } catch (Exception e) {
            log.error("❌ [WebPushSubscriptionController] 모든 웹푸시 구독 해제 실패: userId={}, error={}", 
                    userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (Void) null));
        }
    }

    /**
     * 사용자의 구독 개수를 조회합니다.
     * 
     * @param userNo 인증된 사용자 ID
     * @return 구독 개수
     */
    @GetMapping("/subscriptions/count")
    public ResponseEntity<ApiResponse<Long>> getSubscriptionCount(
            @AuthenticationPrincipal Long userNo) {
        
        log.info("📊 [WebPushSubscriptionController] 웹푸시 구독 개수 조회: userId={}", userNo);

        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [WebPushSubscriptionController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (Long) null));
        }

        try {
            long count = subscriptionService.getSubscriptionCount(userNo);
            log.info("✅ [WebPushSubscriptionController] 웹푸시 구독 개수 조회 성공: userId={}, count={}", 
                    userNo, count);
            return ResponseEntity.ok(ApiResponseGenerator.success(count));

        } catch (Exception e) {
            log.error("❌ [WebPushSubscriptionController] 웹푸시 구독 개수 조회 실패: userId={}, error={}", 
                    userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (Long) null));
        }
    }

    /**
     * 사용자의 모든 구독을 강제로 비활성화합니다.
     * (긴급 상황용 - 일반적인 구독 해제가 작동하지 않을 때 사용)
     * 
     * @param userNo 인증된 사용자 ID
     * @return 강제 비활성화 결과
     */
    @DeleteMapping("/subscriptions/force-deactivate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> forceDeactivateAllSubscriptions(
            @AuthenticationPrincipal Long userNo) {
        
        log.warn("🚨 [WebPushSubscriptionController] 모든 웹푸시 구독 강제 비활성화: userId={}", userNo);
        
        if (userNo == null) {
            log.warn("⚠️ [WebPushSubscriptionController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (Map<String, Object>) null));
        }
        
        try {
            // 강제 비활성화 전 상태 확인
            long beforeCount = subscriptionService.getSubscriptionCount(userNo);
            log.warn("🚨 [WebPushSubscriptionController] 강제 비활성화 전 상태: userId={}, activeCount={}", 
                    userNo, beforeCount);
            
            // 강제 비활성화 실행
            int deactivatedCount = subscriptionService.forceDeactivateAllSubscriptions(userNo);
            
            // 강제 비활성화 후 상태 확인
            long afterCount = subscriptionService.getSubscriptionCount(userNo);
            
            Map<String, Object> result = new HashMap<>();
            result.put("deactivatedCount", deactivatedCount);
            result.put("beforeCount", beforeCount);
            result.put("afterCount", afterCount);
            
            log.warn("🚨 [WebPushSubscriptionController] 강제 비활성화 완료: userId={}, deactivatedCount={}, beforeCount={}, afterCount={}", 
                    userNo, deactivatedCount, beforeCount, afterCount);
            
            return ResponseEntity.ok(ApiResponseGenerator.success(result));
            
        } catch (Exception e) {
            log.error("❌ [WebPushSubscriptionController] 강제 비활성화 실패: userId={}, error={}", 
                    userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (Map<String, Object>) null));
        }
    }
}
