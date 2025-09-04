package site.petful.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.springframework.stereotype.Service;
import site.petful.notificationservice.entity.Notification;
import site.petful.notificationservice.entity.WebPushSubscription;
import site.petful.notificationservice.webpush.VapidProps;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 웹푸시 발송을 담당하는 서비스
 * 
 * VAPID 키를 사용하여 브라우저에 웹푸시를 발송합니다.
 * 비동기 처리를 통해 성능을 최적화합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushService {

    private final VapidProps vapidProps;
    private final WebPushSubscriptionService subscriptionService;
    
    // 비동기 처리를 위한 스레드 풀
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 특정 사용자에게 웹푸시를 발송합니다.
     * 
     * @param userId 사용자 ID
     * @param notification 알림 정보
     * @return 발송 성공 여부
     */
    public boolean sendPushToUser(Long userId, site.petful.notificationservice.entity.Notification notification) {
        log.info("📱 [WebPushService] 사용자에게 웹푸시 발송: userId={}, notificationId={}", userId, notification.getId());

        try {
            // 사용자의 활성화된 구독 정보들을 조회
            List<WebPushSubscription> subscriptions = subscriptionService.getActiveSubscriptions(userId);
            
            if (subscriptions.isEmpty()) {
                log.info("📱 [WebPushService] 활성화된 구독이 없음: userId={}", userId);
                return false;
            }

            log.info("📱 [WebPushService] {}개의 구독에 웹푸시 발송: userId={}", subscriptions.size(), userId);

            // 모든 구독에 비동기로 푸시 발송
            List<CompletableFuture<Boolean>> futures = subscriptions.stream()
                    .map(subscription -> CompletableFuture.supplyAsync(() -> 
                            sendPushToSubscription(subscription, notification), executorService))
                    .toList();

            // 모든 푸시 발송 완료 대기
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));

            allFutures.join();

            // 성공한 푸시 개수 계산
            long successCount = futures.stream()
                    .mapToLong(future -> future.join() ? 1 : 0)
                    .sum();

            boolean success = successCount > 0;
            log.info("📱 [WebPushService] 웹푸시 발송 완료: userId={}, 성공={}/{}, 전체성공={}", 
                    userId, successCount, subscriptions.size(), success);

            return success;

        } catch (Exception e) {
            log.error("❌ [WebPushService] 웹푸시 발송 중 오류: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 특정 구독에 웹푸시를 발송합니다.
     * 
     * @param webPushSubscription 구독 정보
     * @param notification 알림 정보
     * @return 발송 성공 여부
     */
    private boolean sendPushToSubscription(WebPushSubscription webPushSubscription, Notification notification) {
        log.info("📱 [WebPushService] 구독에 웹푸시 발송: subscriptionId={}, endpoint={}", 
                webPushSubscription.getId(), webPushSubscription.getEndpoint());

        try {
            // PushService 인스턴스 생성 (VAPID 키 사용)
            PushService pushService = new PushService();
            pushService.setPrivateKey(vapidProps.getPrivateKey());
            pushService.setPublicKey(vapidProps.getPublicKey());
            pushService.setSubject(vapidProps.getSubject());

            // 구독 정보를 web-push 라이브러리 형식으로 변환
            nl.martijndwars.webpush.Subscription.Keys keys = new nl.martijndwars.webpush.Subscription.Keys(
                    webPushSubscription.getP256dhKey(),
                    webPushSubscription.getAuthKey()
            );
            nl.martijndwars.webpush.Subscription subscription = new nl.martijndwars.webpush.Subscription(
                    webPushSubscription.getEndpoint(),
                    keys
            );

            // 푸시 페이로드 생성
            String payload = createPushPayload(notification);

            // 웹푸시 알림 객체 생성
            nl.martijndwars.webpush.Notification pushNotification = new nl.martijndwars.webpush.Notification(subscription, payload);

            // 웹푸시 발송
            pushService.send(pushNotification);

            // 발송 시간 업데이트
            subscriptionService.updateLastPushTime(webPushSubscription.getId());

            log.info("✅ [WebPushService] 웹푸시 발송 성공: subscriptionId={}", webPushSubscription.getId());
            return true;

        } catch (Exception e) {
            log.error("❌ [WebPushService] 웹푸시 발송 실패: subscriptionId={}, endpoint={}, error={}", 
                    webPushSubscription.getId(), webPushSubscription.getEndpoint(), e.getMessage(), e);
            
            // 구독이 만료되었거나 유효하지 않은 경우 로그만 남김
            if (e.getMessage() != null && (e.getMessage().contains("410") || e.getMessage().contains("Gone"))) {
                log.warn("🗑️ [WebPushService] 만료된 구독 감지: subscriptionId={}, endpoint={}", 
                        webPushSubscription.getId(), webPushSubscription.getEndpoint());
                // TODO: 구독 삭제 기능 추가 필요
            }

            // 구독이 유효하지 않은 경우 비활성화
            if (isInvalidSubscriptionError(e)) {
                log.warn("⚠️ [WebPushService] 유효하지 않은 구독으로 판단하여 비활성화: subscriptionId={}", 
                        webPushSubscription.getId());
                subscriptionService.deactivateSubscriptionByEndpoint(webPushSubscription.getEndpoint());
            }

            return false;
        }
    }

    /**
     * 푸시 페이로드를 생성합니다.
     * 
     * @param notification 알림 정보
     * @return JSON 형태의 페이로드
     */
    private String createPushPayload(site.petful.notificationservice.entity.Notification notification) {
        // 간단한 JSON 페이로드 생성
        // 실제로는 더 복잡한 구조를 사용할 수 있습니다
        return String.format(
                "{\"title\":\"%s\",\"body\":\"%s\",\"icon\":\"/icons/notification-icon.svg\",\"badge\":\"/icons/badge-icon.svg\",\"data\":{\"notificationId\":%d,\"linkUrl\":\"%s\"}}",
                escapeJson(notification.getTitle()),
                escapeJson(notification.getContent()),
                notification.getId(),
                notification.getLinkUrl() != null ? escapeJson(notification.getLinkUrl()) : ""
        );
    }

    /**
     * JSON 문자열에서 특수 문자를 이스케이프합니다.
     * 
     * @param text 원본 텍스트
     * @return 이스케이프된 텍스트
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * 예외가 유효하지 않은 구독으로 인한 것인지 판단합니다.
     * 
     * @param e 예외
     * @return 유효하지 않은 구독 여부
     */
    private boolean isInvalidSubscriptionError(Exception e) {
        String message = e.getMessage().toLowerCase();
        return message.contains("410") || // Gone
               message.contains("invalid") ||
               message.contains("expired") ||
               message.contains("not found");
    }

    /**
     * 서비스 종료 시 스레드 풀을 정리합니다.
     */
    public void shutdown() {
        log.info("🔄 [WebPushService] 스레드 풀 종료 중...");
        executorService.shutdown();
    }
}
