package site.petful.notificationservice.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


import site.petful.notificationservice.dto.NotificationDto;
import site.petful.notificationservice.entity.Notification;
import site.petful.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationReadService {
    private final NotificationRepository notificationRepository;

    public Slice<NotificationDto> listVisible(Long userId, int page, int size){
        int capped = Math.min(Math.max(size,1),30);
        Pageable pageable = PageRequest.of(page,capped, Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
        ));

        return notificationRepository.findByUserIdAndHiddenFalse(userId,pageable).map(NotificationDto::from);
    }

    @Transactional
    public void hide(Long userId, Long notificationId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId가 올바르지 않습니다.");
        }
        if (notificationId == null || notificationId <= 0) {
            throw new IllegalArgumentException("notificationId가 올바르지 않습니다.");
        }
        Notification upadated =  notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(()-> new IllegalArgumentException("알림을 찾을수가 없습니다"));
        if (Boolean.TRUE.equals(upadated.getHidden())) {
            return; // 멱등 처리: 이미 숨김이면 그냥 종료
        }
        upadated.hide();
    }
}
