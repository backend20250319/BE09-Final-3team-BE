package site.petful.snsservice.instagram.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.snsservice.instagram.domain.InstagramInsightEntity;

public interface InstagramInsightRepository extends JpaRepository<InstagramInsightEntity, Long> {

}
