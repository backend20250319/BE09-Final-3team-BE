package site.petful.advertiserservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.advertiserservice.entity.Advertiser;
import site.petful.advertiserservice.entity.advertisement.AdStatus;
import site.petful.advertiserservice.entity.advertisement.Advertisement;

import java.util.List;
import java.util.Optional;

public interface AdRepository extends JpaRepository<Advertisement, Long> {
    Optional<Advertisement> findByAdNo(Long adNo);

    List<Advertisement> findByAdvertiser(Advertiser advertiser);

    List<Advertisement> findByAdvertiserAndAdStatus(Advertiser advertiser, AdStatus adStatus);

    List<Advertisement> findByAdStatus(AdStatus adStatus);
}
