package site.petful.snsservice.instagram.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import site.petful.snsservice.instagram.comment.dto.CommentSearchCondition;
import site.petful.snsservice.instagram.comment.entity.InstagramCommentEntity;

public interface InstagramCommentRepositoryCustom {

    Page<InstagramCommentEntity> searchComments(Long instagramId, CommentSearchCondition condition,
        Pageable pageable);
}
