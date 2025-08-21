package site.petful.advertiserservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.advertiserservice.entity.advertisement.Advertisement;

import java.util.Optional;

public interface AdRepository extends JpaRepository<Advertisement, Long> {
    Optional<Advertisement> findByAdNo(Long adNo);
}
