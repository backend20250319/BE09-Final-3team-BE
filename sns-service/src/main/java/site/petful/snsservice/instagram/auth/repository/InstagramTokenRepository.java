package site.petful.snsservice.instagram.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.snsservice.instagram.auth.entity.InstagramTokenEntity;

@Repository
public interface InstagramTokenRepository extends JpaRepository<InstagramTokenEntity, Long> {


}
