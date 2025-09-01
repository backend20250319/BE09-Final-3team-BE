package site.petful.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.notificationservice.entity.Notification;
import site.petful.notificationservice.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduledNotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryService notificationDeliveryService;

    /**
     * 1분마다 예약된 알림을 체크하고 처리합니다.
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void processScheduledNotifications() {
        log.info("⏰ [ScheduledNotificationService] 예약된 알림 처리 시작");
        
        try {
            // 현재 시간에 발송해야 할 예약 알림들을 조회
            List<Notification> scheduledNotifications = notificationRepository
                    .findByStatusAndScheduledAtBefore(Notification.NotificationStatus.SCHEDULED, LocalDateTime.now());
            
            log.info("📅 [ScheduledNotificationService] 발송할 예약 알림 {}개 발견", scheduledNotifications.size());
            
            for (Notification notification : scheduledNotifications) {
                try {
                    // 알림 발송
                    boolean sent = notificationDeliveryService.sendNotification(notification);
                    
                    if (sent) {
                        notification.markAsSent();
                        log.info("✅ [ScheduledNotificationService] 예약 알림 발송 성공: notificationId={}, userId={}", 
                                notification.getId(), notification.getUserId());
                    } else {
                        notification.markAsFailed();
                        log.error("❌ [ScheduledNotificationService] 예약 알림 발송 실패: notificationId={}, userId={}", 
                                notification.getId(), notification.getUserId());
                    }
                    
                    // 상태 업데이트
                    notificationRepository.save(notification);
                    
                } catch (Exception e) {
                    log.error("❌ [ScheduledNotificationService] 예약 알림 처리 중 오류: notificationId={}, error={}", 
                            notification.getId(), e.getMessage(), e);
                    
                    notification.markAsFailed();
                    notificationRepository.save(notification);
                }
            }
            
        } catch (Exception e) {
            log.error("❌ [ScheduledNotificationService] 예약 알림 처리 중 전체 오류: {}", e.getMessage(), e);
        }
        
        log.info("⏰ [ScheduledNotificationService] 예약된 알림 처리 완료");
    }

    /**
     * 특정 시간에 예약 알림을 생성합니다.
     */
    public Notification scheduleNotification(Long userId, String type, String title, String content, 
                                         String linkUrl, LocalDateTime scheduledAt) {
        log.info("📅 [ScheduledNotificationService] 예약 알림 생성: userId={}, type={}, scheduledAt={}", 
                userId, type, scheduledAt);
        
        Notification notification = Notification.scheduled(userId, type, title, content, linkUrl, scheduledAt);
        Notification savedNotification = notificationRepository.save(notification);
        
        log.info("✅ [ScheduledNotificationService] 예약 알림 생성 완료: notificationId={}", savedNotification.getId());
        
        return savedNotification;
    }
}
