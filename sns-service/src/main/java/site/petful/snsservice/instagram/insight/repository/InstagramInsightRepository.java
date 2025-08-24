package site.petful.snsservice.instagram.insight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.snsservice.instagram.insight.entity.InstagramInsightEntity;

public interface InstagramInsightRepository extends JpaRepository<InstagramInsightEntity, Long> {

}
