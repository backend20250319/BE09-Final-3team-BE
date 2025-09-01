package site.petful.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.notificationservice.dto.EventMessage;
import site.petful.notificationservice.entity.Notification;
import site.petful.notificationservice.repository.NotificationRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryService notificationDeliveryService;

    /**
     * 사용자별 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        log.info("📋 [NotificationService] 사용자 알림 조회: userId={}", userId);
        
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 null입니다.");
        }
        
        try {
            return notificationRepository.findByUserIdAndHiddenFalse(userId, pageable);
        } catch (Exception e) {
            log.error("❌ [NotificationService] 사용자 알림 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("알림 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 알림 상세 조회
     */
    @Transactional(readOnly = true)
    public Notification getNotification(Long notificationId, Long userId) {
        log.info("📋 [NotificationService] 알림 상세 조회: notificationId={}, userId={}", notificationId, userId);
        return notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다."));
    }

    /**
     * 알림 ID로 조회 (테스트용)
     */
    @Transactional(readOnly = true)
    public Notification getNotificationById(Long notificationId) {
        log.info("📋 [NotificationService] 알림 ID로 조회: notificationId={}", notificationId);
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다."));
    }

    /**
     * 알림 숨김 처리
     */
    public void hideNotification(Long notificationId, Long userId) {
        log.info("🙈 [NotificationService] 알림 숨김: notificationId={}, userId={}", notificationId, userId);
        
        Notification notification = getNotification(notificationId, userId);
        notification.hide();
        notificationRepository.save(notification);
        
        log.info("✅ [NotificationService] 알림 숨김 완료: notificationId={}", notificationId);
    }

    /**
     * 즉시 알림 생성 및 발송
     */
    public Notification createImmediateNotification(EventMessage eventMessage) {
        log.info("📝 [NotificationService] 즉시 알림 생성: eventId={}, type={}", 
                eventMessage.getEventId(), eventMessage.getType());

        // 이벤트 타입에 따른 알림 내용 생성
        NotificationContent content = createNotificationContent(eventMessage);
        
        // 알림 엔티티 생성
        Notification notification = Notification.of(
                Long.valueOf(eventMessage.getTarget().getUserId()),
                eventMessage.getType(),
                content.getTitle(),
                content.getContent(),
                content.getLinkUrl()
        );

        // 데이터베이스에 저장
        Notification savedNotification = notificationRepository.save(notification);
        
        log.info("✅ [NotificationService] 즉시 알림 저장 완료: notificationId={}, userId={}", 
                savedNotification.getId(), savedNotification.getUserId());

        // 즉시 알림 발송
        try {
            boolean sent = notificationDeliveryService.sendNotification(savedNotification);
            if (sent) {
                savedNotification.markAsSent();
                notificationRepository.save(savedNotification);
                log.info("✅ [NotificationService] 즉시 알림 발송 성공: notificationId={}", savedNotification.getId());
            } else {
                savedNotification.markAsFailed();
                notificationRepository.save(savedNotification);
                log.error("❌ [NotificationService] 즉시 알림 발송 실패: notificationId={}", savedNotification.getId());
            }
        } catch (Exception e) {
            log.error("❌ [NotificationService] 즉시 알림 발송 중 오류: {}", e.getMessage(), e);
            savedNotification.markAsFailed();
            notificationRepository.save(savedNotification);
        }

        return savedNotification;
    }

    /**
     * 예약 알림 생성
     */
    public Notification createScheduledNotification(EventMessage eventMessage, int delayMinutes) {
        log.info("📅 [NotificationService] 예약 알림 생성: eventId={}, type={}, delayMinutes={}", 
                eventMessage.getEventId(), eventMessage.getType(), delayMinutes);

        // delayMinutes 후에 발송되도록 예약
        LocalDateTime scheduledAt = LocalDateTime.now().plusMinutes(delayMinutes);
        
        // 이벤트 타입에 따른 알림 내용 생성
        NotificationContent content = createNotificationContent(eventMessage);
        
        // 예약 알림 엔티티 생성
        Notification notification = Notification.scheduled(
                Long.valueOf(eventMessage.getTarget().getUserId()),
                eventMessage.getType(),
                content.getTitle(),
                content.getContent(),
                content.getLinkUrl(),
                scheduledAt
        );

        // 데이터베이스에 저장
        Notification savedNotification = notificationRepository.save(notification);
        
        log.info("✅ [NotificationService] 예약 알림 생성 완료: notificationId={}, scheduledAt={}", 
                savedNotification.getId(), savedNotification.getScheduledAt());

        return savedNotification;
    }

    /**
     * 이벤트 타입에 따른 알림 내용을 생성합니다.
     */
    private NotificationContent createNotificationContent(EventMessage eventMessage) {
        String type = eventMessage.getType();
        String actorName = eventMessage.getActor() != null ? eventMessage.getActor().getName() : "알 수 없는 사용자";
        
        // 이벤트 타입별로 다른 알림 내용 생성
        switch (type) {
            case "notification.comment.created":
                return new NotificationContent(
                    "새로운 댓글",
                    actorName + "님이 댓글을 작성했습니다.",
                    "/posts/" + eventMessage.getTarget().getResourceId()
                );
                
            case "notification.post.liked":
                return new NotificationContent(
                    "좋아요",
                    actorName + "님이 게시글을 좋아합니다.",
                    "/posts/" + eventMessage.getTarget().getResourceId()
                );
                
            case "notification.campaign.new":
                return new NotificationContent(
                    "새로운 캠페인",
                    "새로운 캠페인이 등록되었습니다.",
                    "/campaigns/" + eventMessage.getTarget().getResourceId()
                );
                
            case "notification.user.followed":
                return new NotificationContent(
                    "새로운 팔로워",
                    actorName + "님이 회원님을 팔로우하기 시작했습니다.",
                    "/users/" + eventMessage.getActor().getId()
                );
                
            default:
                return new NotificationContent(
                    "새로운 알림",
                    "새로운 알림이 도착했습니다.",
                    null
                );
        }
    }

    /**
     * 알림 내용을 담는 내부 클래스
     */
    private static class NotificationContent {
        private final String title;
        private final String content;
        private final String linkUrl;

        public NotificationContent(String title, String content, String linkUrl) {
            this.title = title;
            this.content = content;
            this.linkUrl = linkUrl;
        }

        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getLinkUrl() { return linkUrl; }
    }
}
