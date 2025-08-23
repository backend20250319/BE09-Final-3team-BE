package site.petful.snsservice.instagram.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;

@Repository
public interface InstagramProfileRepository extends JpaRepository<InstagramProfileEntity, Long> {

}
