package site.petful.snsservice.instagram.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.snsservice.instagram.domain.InstagramProfile;

@Repository
public interface InstagramProfileRepository extends JpaRepository<InstagramProfile, Long> {

}
