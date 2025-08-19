package site.petful.snsservice.instagram.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.snsservice.instagram.domain.InstagramToken;

public interface InstagramRepository extends JpaRepository<InstagramToken, Long> {
    
}
