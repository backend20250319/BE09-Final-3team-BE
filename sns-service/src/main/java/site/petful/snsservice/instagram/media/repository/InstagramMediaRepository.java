package site.petful.snsservice.instagram.media.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.snsservice.instagram.media.entity.InstagramMediaEntity;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;

@Repository
public interface InstagramMediaRepository extends JpaRepository<InstagramMediaEntity, Long> {

    List<InstagramMediaEntity> findAllByInstagramProfile(InstagramProfileEntity profileEntity);

    List<InstagramMediaEntity> findAllByIdIn(List<Long> mediaIds);

    List<InstagramMediaEntity> findTop5ByInstagramProfileOrderByLikeCountDesc(
        InstagramProfileEntity profileEntity);
}

