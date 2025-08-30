package site.petful.advertiserservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.advertiserservice.entity.advertisement.AdFiles;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<AdFiles, Long> {
    Optional<AdFiles> findByAdvertisement_AdNo(Long adNo);
}
