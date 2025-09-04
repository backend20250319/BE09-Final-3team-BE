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



    Page<Notification> findByUserIdAndHiddenFalse(Long userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Long notificationId, Long userId);


    long countByUserIdAndIsReadFalseAndHiddenFalse(Long userId);


    Page<Notification> findByUserIdAndIsReadFalseAndHiddenFalse(Long userId, Pageable pageable);

    // 예약된 알림 조회 (스케줄러용)
    List<Notification> findByStatusAndScheduledAtBefore(Notification.NotificationStatus status, LocalDateTime scheduledAt);

}
