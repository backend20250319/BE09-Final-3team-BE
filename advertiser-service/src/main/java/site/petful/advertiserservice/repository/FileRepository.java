package site.petful.advertiserservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.advertiserservice.entity.advertiser.AdvertiserFiles;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<AdvertiserFiles, Long> {

    Optional<List<AdvertiserFiles>> findByAdvertiser_AdvertiserNo(Long advertiserNo);
}
