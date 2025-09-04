package site.petful.notificationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.petful.notificationservice.entity.WebPushSubscription;

import java.util.List;
import java.util.Optional;

/**
 * 웹푸시 구독 정보를 관리하는 Repository
 * 
 * 사용자의 구독 정보를 저장, 조회, 관리하는 기능을 제공합니다.
 */
@Repository
public interface WebPushSubscriptionRepository extends JpaRepository<WebPushSubscription, Long> {

    /**
     * 특정 사용자의 활성화된 구독 정보들을 조회
     * 
     * @param userId 사용자 ID
     * @return 활성화된 구독 정보 목록
     */
    List<WebPushSubscription> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * 특정 사용자의 모든 구독 정보를 조회 (활성화/비활성화 포함)
     * 
     * @param userId 사용자 ID
     * @return 모든 구독 정보 목록
     */
    List<WebPushSubscription> findByUserId(Long userId);

    /**
     * 엔드포인트로 구독 정보 조회
     * 
     * @param endpoint 구독 엔드포인트
     * @return 구독 정보 (Optional)
     */
    Optional<WebPushSubscription> findByEndpoint(String endpoint);

    /**
     * 사용자와 엔드포인트로 구독 정보 조회
     * 
     * @param userId 사용자 ID
     * @param endpoint 구독 엔드포인트
     * @return 구독 정보 (Optional)
     */
    Optional<WebPushSubscription> findByUserIdAndEndpoint(Long userId, String endpoint);

    /**
     * 특정 사용자의 구독 개수 조회
     * 
     * @param userId 사용자 ID
     * @return 활성화된 구독 개수
     */
    long countByUserIdAndIsActiveTrue(Long userId);

    /**
     * 비활성화된 구독 정보들을 조회 (정리용)
     * 
     * @return 비활성화된 구독 정보 목록
     */
    @Query("SELECT s FROM WebPushSubscription s WHERE s.isActive = false")
    List<WebPushSubscription> findInactiveSubscriptions();

    /**
     * 오래된 비활성화 구독 정보들을 조회 (정리용)
     * 
     * @param daysAgo 며칠 전부터
     * @return 오래된 비활성화 구독 정보 목록
     */
    @Query("SELECT s FROM WebPushSubscription s WHERE s.isActive = false AND s.updatedAt < :cutoffDate")
    List<WebPushSubscription> findOldInactiveSubscriptions(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
