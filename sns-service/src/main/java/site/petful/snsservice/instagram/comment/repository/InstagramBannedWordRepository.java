package site.petful.snsservice.instagram.comment.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.snsservice.instagram.comment.entity.InstagramBannedWordEntity;
import site.petful.snsservice.instagram.comment.entity.InstagramBannedWordId;

@Repository
public interface InstagramBannedWordRepository extends
    JpaRepository<InstagramBannedWordEntity, InstagramBannedWordId>,
    InstagramBannedWordRepositoryCustom {

    List<InstagramBannedWordEntity> findById_InstagramId(Long instagramId);

}
