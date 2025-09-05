package site.petful.communityservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.petful.communityservice.entity.Post;
import site.petful.communityservice.entity.PostStatus;
import site.petful.communityservice.entity.PostType;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {

    Page<Post> findByStatusAndType(PostStatus postStatus, Pageable pageable, PostType type);

    Page<Post> findByUserIdAndStatus(Long userNo, PostStatus postStatus, Pageable pageable);

    Page<Post> findByUserIdAndStatusAndType(Long userNo, PostStatus postStatus, Pageable pageable, PostType type);

    Page<Post> findByStatus(PostStatus postStatus, Pageable pageable);
    
    @Query("SELECT p.userId FROM Post p WHERE p.id = :postId")
    Long findUserIdByPostId(@Param("postId") Long postId);
}
