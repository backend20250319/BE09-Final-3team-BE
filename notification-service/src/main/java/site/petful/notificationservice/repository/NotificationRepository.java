package site.petful.notificationservice.repository;

import org.springframework.data.domain.Slice;
import site.petful.notificationservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {



    // SENT 상태이고 숨김처리되지 않은 알림만 조회
    Page<Notification> findByUserIdAndStatusAndHiddenFalse(Long userId, Notification.NotificationStatus status, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Long notificationId, Long userId);

    // 읽지 않은 알림 개수 조회 (SENT 상태만)
    long countByUserIdAndStatusAndIsReadFalseAndHiddenFalse(Long userId, Notification.NotificationStatus status);

    // 예약된 알림 조회 (스케줄러용)
    List<Notification> findByStatusAndScheduledAtBefore(Notification.NotificationStatus status, LocalDateTime scheduledAt);

    // 읽지 않은 알림 조회 (SENT 상태만)
    Page<Notification> findByUserIdAndStatusAndIsReadFalseAndHiddenFalse(Long userId, Notification.NotificationStatus status, Pageable pageable);
}
