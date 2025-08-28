package site.petful.communityservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.communityservice.entity.Comment;
import site.petful.communityservice.entity.CommentStatus;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    boolean existsByParentId(Long parentId);

    List<PostId> findByPostIdInAndCommentStatus(List<Long> postIds, CommentStatus commentStatus);

    int countByPostId(Long postId);

    Page<Comment> findByPostIdAndParentIdIsNull(Long postId, Pageable pageable);

    List<Comment> findByPostIdAndParentIdIn(Long postId, List<Long> rootIds);


    interface PostId {
        long getPostId();
    }
}
