package com.lhw.notificationservice.service;

import com.lhw.notificationservice.entity.Notification;
import com.lhw.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationReadService {
    private final NotificationRepository notificationRepository;

    public Slice<Notification> listVisible(Long userId, int page, int size){
        return notificationRepository.findVisibleByUser(userId, PageRequest.of(page, size));
    }

    @Transactional
    public void hide(Long userId, Long notificationId){
      notificationRepository.hideByIdAndUser(notificationId,userId);
    }
}
