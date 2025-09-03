package site.petful.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.petful.notificationservice.entity.Notification;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDeliveryService {

    /**
     * 알림을 발송합니다.
     * @param notification 발송할 알림
     * @return 발송 성공 여부
     */
    public boolean sendNotification(Notification notification) {
        log.info("📤 [NotificationDeliveryService] 알림 발송 시작: notificationId={}, userId={}, type={}", 
                notification.getId(), notification.getUserId(), notification.getType());

        try {
            // 2. 웹 푸시 알림 발송 (웹 브라우저)
            boolean webPushSent = sendWebPushNotification(notification);
            
            boolean success = webPushSent;
            
            if (success) {
                log.info("✅ [NotificationDeliveryService] 알림 발송 성공: notificationId={}, push={}, webPush={}, email={}, sms={}", 
                        notification.getId(),  webPushSent);
            } else {
                log.warn("⚠️ [NotificationDeliveryService] 모든 채널에서 알림 발송 실패: notificationId={}", notification.getId());
            }
            
            return success;
            
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
            // TODO: 웹 푸시 알림 서비스 연동
            log.info("🌐 [NotificationDeliveryService] 웹 푸시 알림 발송: userId={}, title={}", 
                    notification.getUserId(), notification.getTitle());
            
            // 임시로 성공 반환 (실제 구현 시에는 웹 푸시 서비스 API 호출)
            return true;
            
        } catch (Exception e) {
            log.error("❌ [NotificationDeliveryService] 웹 푸시 알림 발송 실패: {}", e.getMessage(), e);
            return false;
        }
    }

}
