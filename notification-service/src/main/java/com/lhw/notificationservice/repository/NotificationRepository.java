package com.lhw.notificationservice.repository;

import com.lhw.notificationservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("""
    select n from Notification n
    where n.userId = :userId and n.hidden = false
    order by n.createdAt desc
  """)
    Page<Notification> findVisibleByUser(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Query("""
    update Notification n
    set n.hidden = true, n.hiddenAt = CURRENT_TIMESTAMP
    where n.id = :id and n.userId = :userId and n.hidden = false
  """)
    int hideByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
}
