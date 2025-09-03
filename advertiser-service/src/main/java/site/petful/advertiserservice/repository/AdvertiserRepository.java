package site.petful.advertiserservice.repository;

import io.lettuce.core.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.advertiserservice.dto.advertiser.AdvertiserResponse;
import site.petful.advertiserservice.entity.advertiser.Advertiser;

import java.util.Optional;

public interface AdvertiserRepository extends JpaRepository<Advertiser, Long> {

    Optional<Advertiser> findByAdvertiserNo(Long advertiserNo);

    boolean existsByUserId(String userId);

    Optional<Advertiser> findByUserId(String userId);

    Page<Advertiser> findByIsApprovedFalseAndReasonIsNotNull(Pageable pageable);
}
