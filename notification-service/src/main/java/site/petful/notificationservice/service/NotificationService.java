package site.petful.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.notificationservice.dto.EventMessage;
import site.petful.notificationservice.entity.Notification;
import site.petful.notificationservice.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryService notificationDeliveryService;

    /**
     * 사용자별 알림 목록 조회 (최신순 정렬)
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        log.info("📋 [NotificationService] 사용자 알림 조회: userId={}", userId);
        
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 null입니다.");
        }
        
        try {
            // createdAt 우선, id 보조 정렬
            Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(), 
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
            );
            
            return notificationRepository.findByUserIdAndHiddenFalse(userId, sortedPageable);
        } catch (Exception e) {
            log.error("❌ [NotificationService] 사용자 알림 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("알림 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {
        log.info("🔢 [NotificationService] 읽지 않은 알림 개수 조회: userId={}", userId);
        
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 null입니다.");
        }
        
        try {
            return notificationRepository.countByUserIdAndIsReadFalseAndHiddenFalse(userId);
        } catch (Exception e) {
            log.error("❌ [NotificationService] 읽지 않은 알림 개수 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("알림 개수 조회 중 오류가 발생했습니다.", e);
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
     * 알림 읽음 처리
     */
    public void markNotificationAsRead(Long notificationId, Long userId) {
        log.info("👁️ [NotificationService] 알림 읽음 처리: notificationId={}, userId={}", notificationId, userId);
        
        Notification notification = getNotification(notificationId, userId);
        notification.markAsRead();
        notificationRepository.save(notification);
        
        log.info("✅ [NotificationService] 알림 읽음 처리 완료: notificationId={}", notificationId);
    }

    /**
     * 모든 알림 읽음 처리
     */
    public void markAllNotificationsAsRead(Long userId) {
        log.info("👁️ [NotificationService] 모든 알림 읽음 처리: userId={}", userId);
        
        try {
            Pageable pageable = PageRequest.of(0, 1000); // 한 번에 처리할 최대 개수
            Page<Notification> unreadNotifications = notificationRepository
                    .findByUserIdAndIsReadFalseAndHiddenFalse(userId, pageable);
            
            for (Notification notification : unreadNotifications.getContent()) {
                notification.markAsRead();
            }
            
            notificationRepository.saveAll(unreadNotifications.getContent());
            log.info("✅ [NotificationService] {}개의 알림을 읽음 처리 완료", unreadNotifications.getContent().size());
            
        } catch (Exception e) {
            log.error("❌ [NotificationService] 모든 알림 읽음 처리 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("알림 읽음 처리 중 오류가 발생했습니다.", e);
        }
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
    public Notification createScheduledNotification(EventMessage eventMessage, String timeStr) {
        log.info("📅 [NotificationService] 예약 알림 생성: eventId={}, type={}, timeStr={}", 
                eventMessage.getEventId(), eventMessage.getType(), timeStr);

        // timeStr이 null이거나 빈 문자열인 경우 처리
        if (timeStr == null || timeStr.trim().isEmpty()) {
            log.warn("⚠️ [NotificationService] timeStr이 null이거나 빈 문자열입니다. 즉시 알림으로 생성합니다. timeStr={}", timeStr);
            return createImmediateNotification(eventMessage);
        }

        // timeStr을 파싱하여 LocalTime으로 변환
        LocalTime targetTime;
        try {
            targetTime = LocalTime.parse(timeStr.trim());
        } catch (Exception e) {
            log.error("❌ [NotificationService] timeStr 파싱 실패: timeStr={}, error={}", timeStr, e.getMessage());
            return createImmediateNotification(eventMessage);
        }

        // timeStr에 있는 시간을 그대로 사용하여 scheduledAt 생성 (한국 시간대 사용)
        LocalDateTime scheduledAt = LocalDate.now(ZoneId.of("Asia/Seoul")).atTime(targetTime);
        
        // 디버깅을 위한 로그 추가
        log.info("🔍 [NotificationService] 시간 파싱 결과: timeStr={}, targetTime={}, scheduledAt={}", 
                timeStr, targetTime, scheduledAt);
        
        // 시간이 제대로 설정되었는지 확인
        if (scheduledAt.getHour() != targetTime.getHour() || scheduledAt.getMinute() != targetTime.getMinute()) {
            log.error("❌ [NotificationService] 시간 설정 오류: timeStr={}, targetTime={}, scheduledAt={}", 
                    timeStr, targetTime, scheduledAt);
        }
        
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
        
        log.info("✅ [NotificationService] 예약 알림 생성 완료: notificationId={}, timeStr={}, scheduledAt={}", 
                savedNotification.getId(), timeStr, savedNotification.getScheduledAt());

        return savedNotification;
    }

    /**
     * 예약 알림 생성 (LocalDateTime 기반)
     */
    public Notification createScheduledNotification(EventMessage eventMessage, LocalDateTime scheduledTime) {
        log.info("📅 [NotificationService] 예약 알림 생성 (LocalDateTime): eventId={}, type={}, scheduledTime={}", 
                eventMessage.getEventId(), eventMessage.getType(), scheduledTime);

        if (scheduledTime == null) {
            log.warn("⚠️ [NotificationService] scheduledTime이 null입니다. 즉시 알림으로 생성합니다.");
            return createImmediateNotification(eventMessage);
        }

        // scheduledTime을 그대로 사용 (시간대 변환 없이)
        log.info("🔍 [NotificationService] scheduledTime을 그대로 사용: {}", scheduledTime);
        log.info("🔍 [NotificationService] scheduledTime 상세: year={}, month={}, day={}, hour={}, minute={}, second={}", 
                scheduledTime.getYear(), scheduledTime.getMonth(), scheduledTime.getDayOfMonth(), 
                scheduledTime.getHour(), scheduledTime.getMinute(), scheduledTime.getSecond());
        
        // 이벤트 타입에 따른 알림 내용 생성
        NotificationContent content = createNotificationContent(eventMessage);
        
        // 예약 알림 엔티티 생성
        log.info("🔍 [NotificationService] 엔티티 생성 전 scheduledTime: {}", scheduledTime);
        
        Notification notification = Notification.scheduled(
                Long.valueOf(eventMessage.getTarget().getUserId()),
                eventMessage.getType(),
                content.getTitle(),
                content.getContent(),
                content.getLinkUrl(),
                scheduledTime
        );
        
        log.info("🔍 [NotificationService] 엔티티 생성 후 scheduledAt: {}", notification.getScheduledAt());

        // 데이터베이스에 저장
        log.info("🔍 [NotificationService] DB 저장 직전 notification.scheduledAt: {}", notification.getScheduledAt());
        Notification savedNotification = notificationRepository.save(notification);
        log.info("🔍 [NotificationService] DB 저장 직후 savedNotification.scheduledAt: {}", savedNotification.getScheduledAt());
        
        log.info("✅ [NotificationService] 예약 알림 생성 완료: notificationId={}, 원본시간={}, DB저장={}", 
                savedNotification.getId(), scheduledTime, savedNotification.getScheduledAt());

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
                
            case "health.schedule":
                String scheduleMessage = (String) eventMessage.getAttributes().get("message");
                return new NotificationContent(
                    "새로운 건강 일정",
                    scheduleMessage != null ? scheduleMessage : "새로운 건강 일정이 등록되었습니다.",
                    "/schedules/" + eventMessage.getTarget().getResourceId()
                );
            case "health.schedule.reserve":
                String reserveMessage = (String) eventMessage.getAttributes().get("message");
                return new NotificationContent(
                        "건강 알림",
                        reserveMessage != null ? reserveMessage : "건강 일정 알림입니다.",
                        "/schedules/" + eventMessage.getTarget().getResourceId()
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
     * 읽지 않은 알림 개수 조회
     */
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {
        log.info("🔢 [NotificationService] 읽지 않은 알림 개수 조회: userId={}", userId);
        
        try {
            long count = notificationRepository.countByUserIdAndIsReadFalseAndHiddenFalse(userId);
            log.info("✅ [NotificationService] 읽지 않은 알림 개수: {}", count);
            return count;
        } catch (Exception e) {
            log.error("❌ [NotificationService] 읽지 않은 알림 개수 조회 실패: {}", e.getMessage(), e);
            return 0;
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
