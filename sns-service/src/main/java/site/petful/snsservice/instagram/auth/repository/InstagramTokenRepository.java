package site.petful.snsservice.instagram.auth.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.petful.snsservice.instagram.auth.entity.InstagramTokenEntity;

@Repository
public interface InstagramTokenRepository extends JpaRepository<InstagramTokenEntity, Long> {


    int deleteByExpireAtBefore(LocalDateTime now);

    @Query("SELECT i.userNo FROM InstagramTokenEntity i")
    List<Long> findAllUserNos();
}
