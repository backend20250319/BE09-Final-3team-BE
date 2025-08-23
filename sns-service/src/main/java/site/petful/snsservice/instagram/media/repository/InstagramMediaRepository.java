package site.petful.snsservice.instagram.media.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.snsservice.instagram.media.entity.InstagramMediaEntity;

@Repository
public interface InstagramMediaRepository extends JpaRepository<InstagramMediaEntity, Long> {

}
