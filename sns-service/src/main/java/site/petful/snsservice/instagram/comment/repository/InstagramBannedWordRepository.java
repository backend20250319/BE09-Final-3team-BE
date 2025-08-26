package site.petful.snsservice.instagram.comment.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.petful.snsservice.instagram.comment.entity.InstagramBannedWordEntity;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;

@Repository
public interface InstagramBannedWordRepository extends
    JpaRepository<InstagramBannedWordEntity, String> {


    @Modifying
    @Query("DELETE FROM InstagramBannedWordEntity b WHERE b.instagramProfile.id = :instagramId AND b.instagramBannedWordId.word = :word")
    int deleteByInstagramProfileIdAndWord(@Param("instagramId") Long instagramId,
        @Param("word") String word);

    List<InstagramBannedWordEntity> findByInstagramProfile(InstagramProfileEntity instagramProfile);
}
