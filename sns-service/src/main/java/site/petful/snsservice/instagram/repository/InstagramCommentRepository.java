package site.petful.snsservice.instagram.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.snsservice.instagram.domain.InstagramCommentEntity;

public interface InstagramCommentRepository extends JpaRepository<InstagramCommentEntity, Long> {

}
