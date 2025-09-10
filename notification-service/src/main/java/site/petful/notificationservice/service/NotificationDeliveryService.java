package site.petful.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.petful.notificationservice.entity.Notification;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDeliveryService {

    private final WebPushService webPushService;

    @Value("${app.notification.webpush.enabled:true}")
    private boolean webPushEnabled;
    /**
     * 알림을 발송합니다.
     * @param notification 발송할 알림
     * @return 발송 성공 여부
     */
    public boolean sendNotification(Notification notification) {
        log.info("📤 [NotificationDeliveryService] 알림 발송 시작: notificationId={}, userId={}, type={}", 
                notification.getId(), notification.getUserId(), notification.getType());

        try {
            // 웹 푸시 알림 발송 시도
            boolean webPushSent = sendWebPushNotification(notification);
            
            // 웹푸시 발송 실패 시 알림도 실패로 처리 (구독이 없으면 알림 생성 안함)
            if (webPushSent) {
                log.info("✅ [NotificationDeliveryService] 알림 발송 성공 (웹푸시 포함): notificationId={}, webPush={}", 
                        notification.getId(), webPushSent);
                return true;
            } else {
                log.warn("⚠️ [NotificationDeliveryService] 웹푸시 발송 실패로 인한 알림 발송 실패: notificationId={}, webPush={}", 
                        notification.getId(), webPushSent);
                return false;
            }
            
        } catch (Exception e) {
            log.error("❌ [NotificationDeliveryService] 알림 발송 중 오류: notificationId={}, error={}", 
                    notification.getId(), e.getMessage(), e);
            return false;
        }
    }


    /**
     * 웹 푸시 알림 발송 (웹 브라우저)
     */
    private boolean sendWebPushNotification(Notification notification) {
        try {
            if (!webPushEnabled) {
                log.info("🌐 [NotificationDeliveryService] 웹푸시가 비활성화됨: userId={}", notification.getUserId());
                return true; // 비활성화된 경우 성공으로 처리
            }

            log.info("🌐 [NotificationDeliveryService] 웹 푸시 알림 발송: userId={}, title={}", 
                    notification.getUserId(), notification.getTitle());
            
            // 실제 웹푸시 서비스 호출
            boolean success = webPushService.sendPushToUser(notification.getUserId(), notification);
            
            if (success) {
                log.info("✅ [NotificationDeliveryService] 웹푸시 발송 성공: userId={}, notificationId={}", 
                        notification.getUserId(), notification.getId());
            } else {
                log.warn("⚠️ [NotificationDeliveryService] 웹푸시 발송 실패 (구독 없음 또는 오류): userId={}, notificationId={}", 
                        notification.getUserId(), notification.getId());
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("❌ [NotificationDeliveryService] 웹 푸시 알림 발송 실패: userId={}, error={}", 
                    notification.getUserId(), e.getMessage(), e);
            return false;
        }
    }

}
