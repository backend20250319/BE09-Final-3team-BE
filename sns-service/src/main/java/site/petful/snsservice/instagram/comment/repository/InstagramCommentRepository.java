package site.petful.snsservice.instagram.comment.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.snsservice.instagram.comment.entity.InstagramCommentEntity;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;

public interface InstagramCommentRepository extends JpaRepository<InstagramCommentEntity, Long>,
    InstagramCommentRepositoryCustom {

    List<InstagramCommentEntity> findAllByIdIn(Collection<Long> ids);

    long countByInstagramProfile(InstagramProfileEntity profile);

    long countByInstagramProfileAndIsDeleted(InstagramProfileEntity profile, boolean b);

    List<InstagramCommentEntity> findInstagramCommentEntitiesByInstagramProfile(
        InstagramProfileEntity profile);
}
