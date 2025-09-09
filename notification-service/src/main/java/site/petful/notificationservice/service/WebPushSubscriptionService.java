package site.petful.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.notificationservice.entity.WebPushSubscription;
import site.petful.notificationservice.repository.WebPushSubscriptionRepository;

import java.util.List;
import java.util.Optional;

/**
 * 웹푸시 구독 정보를 관리하는 서비스
 * 
 * 사용자의 구독 정보를 저장, 조회, 관리하는 비즈니스 로직을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WebPushSubscriptionService {

    private final WebPushSubscriptionRepository subscriptionRepository;

    /**
     * 새로운 구독 정보를 저장합니다.
     * 
     * @param userId 사용자 ID
     * @param endpoint 구독 엔드포인트
     * @param p256dhKey P-256 공개키
     * @param authKey 인증 키
     * @param userAgent 사용자 에이전트
     * @return 저장된 구독 정보
     */
    public WebPushSubscription saveSubscription(Long userId, String endpoint, String p256dhKey, String authKey, String userAgent) {
        log.info("💾 [WebPushSubscriptionService] 구독 정보 저장: userId={}, endpoint={}", userId, endpoint);

        // 기존 구독 정보가 있는지 확인
        Optional<WebPushSubscription> existingSubscription = subscriptionRepository.findByUserIdAndEndpoint(userId, endpoint);
        
        if (existingSubscription.isPresent()) {
            // 기존 구독 정보가 있으면 활성화하고 업데이트
            WebPushSubscription subscription = existingSubscription.get();
            subscription.setP256dhKey(p256dhKey);
            subscription.setAuthKey(authKey);
            subscription.setUserAgent(userAgent);
            subscription.activate();
            
            WebPushSubscription savedSubscription = subscriptionRepository.save(subscription);
            log.info("✅ [WebPushSubscriptionService] 기존 구독 정보 업데이트: subscriptionId={}", savedSubscription.getId());
            return savedSubscription;
        } else {
            // 새로운 구독 정보 생성
            WebPushSubscription subscription = WebPushSubscription.of(userId, endpoint, p256dhKey, authKey, userAgent);
            WebPushSubscription savedSubscription = subscriptionRepository.save(subscription);
            log.info("✅ [WebPushSubscriptionService] 새로운 구독 정보 저장: subscriptionId={}", savedSubscription.getId());
            return savedSubscription;
        }
    }

    /**
     * 사용자의 활성화된 구독 정보들을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 활성화된 구독 정보 목록
     */
    @Transactional(readOnly = true)
    public List<WebPushSubscription> getActiveSubscriptions(Long userId) {
        log.info("📋 [WebPushSubscriptionService] 활성화된 구독 정보 조회: userId={}", userId);
        
        List<WebPushSubscription> subscriptions = subscriptionRepository.findByUserIdAndIsActiveTrue(userId);
        log.info("✅ [WebPushSubscriptionService] 활성화된 구독 {}개 조회: userId={}", subscriptions.size(), userId);
        
        // 디버깅을 위해 각 구독의 상세 정보 로그
        for (WebPushSubscription subscription : subscriptions) {
            log.info("📋 [WebPushSubscriptionService] 활성 구독 상세: subscriptionId={}, endpoint={}, isActive={}, createdAt={}, updatedAt={}", 
                    subscription.getId(), subscription.getEndpoint(), subscription.getIsActive(), 
                    subscription.getCreatedAt(), subscription.getUpdatedAt());
        }
        
        return subscriptions;
    }

    /**
     * 사용자의 모든 구독 정보를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 모든 구독 정보 목록
     */
    @Transactional(readOnly = true)
    public List<WebPushSubscription> getAllSubscriptions(Long userId) {
        log.info("📋 [WebPushSubscriptionService] 모든 구독 정보 조회: userId={}", userId);
        
        List<WebPushSubscription> subscriptions = subscriptionRepository.findByUserId(userId);
        log.info("✅ [WebPushSubscriptionService] 구독 {}개 조회: userId={}", subscriptions.size(), userId);
        
        return subscriptions;
    }

    /**
     * 구독 정보를 비활성화합니다.
     * 
     * @param subscriptionId 구독 ID
     * @param userId 사용자 ID (보안 검증용)
     * @return 비활성화 성공 여부
     */
    public boolean deactivateSubscription(Long subscriptionId, Long userId) {
        log.info("🚫 [WebPushSubscriptionService] 구독 비활성화: subscriptionId={}, userId={}", subscriptionId, userId);
        
        Optional<WebPushSubscription> subscriptionOpt = subscriptionRepository.findById(subscriptionId);
        
        if (subscriptionOpt.isEmpty()) {
            log.warn("⚠️ [WebPushSubscriptionService] 구독 정보를 찾을 수 없음: subscriptionId={}", subscriptionId);
            return false;
        }
        
        WebPushSubscription subscription = subscriptionOpt.get();
        
        // 사용자 ID 검증
        if (!subscription.getUserId().equals(userId)) {
            log.warn("⚠️ [WebPushSubscriptionService] 구독 소유자가 아님: subscriptionId={}, userId={}, ownerId={}", 
                    subscriptionId, userId, subscription.getUserId());
            return false;
        }
        
        subscription.deactivate();
        subscriptionRepository.save(subscription);
        
        log.info("✅ [WebPushSubscriptionService] 구독 비활성화 완료: subscriptionId={}", subscriptionId);
        return true;
    }

    /**
     * 엔드포인트로 구독 정보를 비활성화합니다.
     * 
     * @param endpoint 구독 엔드포인트
     * @return 비활성화 성공 여부
     */
    public boolean deactivateSubscriptionByEndpoint(String endpoint) {
        log.info("🚫 [WebPushSubscriptionService] 엔드포인트로 구독 비활성화: endpoint={}", endpoint);
        
        Optional<WebPushSubscription> subscriptionOpt = subscriptionRepository.findByEndpoint(endpoint);
        
        if (subscriptionOpt.isEmpty()) {
            log.warn("⚠️ [WebPushSubscriptionService] 엔드포인트로 구독 정보를 찾을 수 없음: endpoint={}", endpoint);
            return false;
        }
        
        WebPushSubscription subscription = subscriptionOpt.get();
        subscription.deactivate();
        subscriptionRepository.save(subscription);
        
        log.info("✅ [WebPushSubscriptionService] 엔드포인트로 구독 비활성화 완료: subscriptionId={}", subscription.getId());
        return true;
    }

    /**
     * 사용자의 구독 개수를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 활성화된 구독 개수
     */
    @Transactional(readOnly = true)
    public long getSubscriptionCount(Long userId) {
        long count = subscriptionRepository.countByUserIdAndIsActiveTrue(userId);
        log.info("📊 [WebPushSubscriptionService] 구독 개수: userId={}, count={}", userId, count);
        return count;
    }

    /**
     * 푸시 발송 시간을 업데이트합니다.
     * 
     * @param subscriptionId 구독 ID
     */
    public void updateLastPushTime(Long subscriptionId) {
        Optional<WebPushSubscription> subscriptionOpt = subscriptionRepository.findById(subscriptionId);
        
        if (subscriptionOpt.isPresent()) {
            WebPushSubscription subscription = subscriptionOpt.get();
            subscription.updateLastPushTime();
            subscriptionRepository.save(subscription);
            log.debug("🕐 [WebPushSubscriptionService] 푸시 발송 시간 업데이트: subscriptionId={}", subscriptionId);
        }
    }
    
    /**
     * 사용자의 모든 구독을 강제로 비활성화합니다.
     * (긴급 상황용)
     * 
     * @param userId 사용자 ID
     * @return 비활성화된 구독 개수
     */
    @Transactional
    public int forceDeactivateAllSubscriptions(Long userId) {
        log.warn("🚨 [WebPushSubscriptionService] 사용자의 모든 구독 강제 비활성화: userId={}", userId);
        
        List<WebPushSubscription> allSubscriptions = subscriptionRepository.findByUserId(userId);
        int deactivatedCount = 0;
        
        for (WebPushSubscription subscription : allSubscriptions) {
            if (subscription.getIsActive()) {
                subscription.deactivate();
                subscriptionRepository.save(subscription);
                deactivatedCount++;
                log.info("🚨 [WebPushSubscriptionService] 구독 강제 비활성화: subscriptionId={}, userId={}", 
                        subscription.getId(), userId);
            }
        }
        
        log.warn("🚨 [WebPushSubscriptionService] 강제 비활성화 완료: userId={}, deactivatedCount={}", 
                userId, deactivatedCount);
        
        return deactivatedCount;
    }
}
