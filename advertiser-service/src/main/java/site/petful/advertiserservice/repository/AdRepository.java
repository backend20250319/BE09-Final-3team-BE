package site.petful.advertiserservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.petful.advertiserservice.entity.advertiser.Advertiser;
import site.petful.advertiserservice.entity.advertisement.AdStatus;
import site.petful.advertiserservice.entity.advertisement.Advertisement;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdRepository extends JpaRepository<Advertisement, Long> {
    Optional<Advertisement> findByAdNo(Long adNo);

    List<Advertisement> findByAdvertiser(Advertiser advertiser);

    List<Advertisement> findByAdvertiserAndAdStatus(Advertiser advertiser, AdStatus adStatus);

    List<Advertisement> findByAdStatus(AdStatus adStatus);

    Page<Advertisement> findByAdStatus(AdStatus adStatus, Pageable pageable);

    @Modifying
    @Query("UPDATE Advertisement a SET a.applicants = a.applicants + :incrementBy WHERE a.adNo = :adNo")
    int incrementApplicants(@Param("adNo") Long adNo, @Param("incrementBy") int incrementBy);

    List<Advertisement> findByAnnounceEndAndAdStatus(LocalDate today, AdStatus adStatus);

    List<Advertisement> findByCampaignSelectAndAdStatus(LocalDate today, AdStatus adStatus);

    List<Advertisement> findByCampaignStartAndAdStatus(LocalDate today, AdStatus adStatus);

    List<Advertisement> findByCampaignEndAndAdStatus(LocalDate targetDate, AdStatus adStatus);
}
