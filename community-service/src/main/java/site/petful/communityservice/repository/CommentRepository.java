package site.petful.communityservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.communityservice.entity.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    int countByPostId(Long id);

    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    boolean existsByParentId(Long parentId);

}
