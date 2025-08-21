package site.petful.snsservice.instagram.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.snsservice.instagram.domain.InstagramToken;

@Repository
public interface InstagramTokenRepository extends JpaRepository<InstagramToken, Long> {


    Optional<InstagramToken> findByUserId(Long userId);
}
