package site.petful.advertiserservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.advertiserservice.entity.advertisement.Advertisement;

public interface AdRepository extends JpaRepository<Advertisement, Long> {
}
