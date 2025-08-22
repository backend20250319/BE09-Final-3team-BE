package site.petful.communityservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.communityservice.entity.Post;
import site.petful.communityservice.entity.PostType;

@Repository
public interface CommunityRepository extends JpaRepository<Post,Long> {
    Page<Post> findByUserId(Long userNo, Pageable pageable);

    Page<Post> findByUserIdAndType(Long userNo, PostType type, Pageable pageable);
}
