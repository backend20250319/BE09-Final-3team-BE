package site.petful.snsservice.instagram.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.snsservice.instagram.comment.entity.InstagramCommentEntity;

public interface InstagramCommentRepository extends JpaRepository<InstagramCommentEntity, Long> {

}
