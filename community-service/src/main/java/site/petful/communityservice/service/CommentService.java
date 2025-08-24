package site.petful.communityservice.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.communityservice.dto.CommentCreateRequest;
import site.petful.communityservice.dto.CommentCreateResponse;
import site.petful.communityservice.entity.Comment;
import site.petful.communityservice.entity.CommentStatus;
import site.petful.communityservice.repository.CommentRepository;
import site.petful.communityservice.repository.PostRepository;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public CommentCreateResponse createComment(Long userNo, CommentCreateRequest request) {
        if(!postRepository.existsById(request.getPostId())) {
            throw new NotFoundException("해당 게시물이 존재하지 않습니다.");
        }
        Long parentId = request.getParentId();
        if(parentId != null && !commentRepository.existsById(parentId)) {
            throw new IllegalArgumentException("댓글이 존재하지 않습니다.");
        }
        Comment c = Comment.builder()
                .postId(request.getPostId())
                .parentId(parentId)
                .postId(request.getPostId())
                .content(request.getContent().trim())
                .createdAt(LocalDateTime.now())
                .build();
        Comment saved = commentRepository.save(c);

        return new CommentCreateResponse(
                saved.getId(),
                saved.getPostId(),
                saved.getParentId(),
                saved.getUserId(),
                saved.getCreatedAt()
        );
    }

    public Boolean deleteComment(Long userNo, Long commentId, String userType) throws AccessDeniedException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("삭제할 댓글을 찾지 못했습니다."));
        if(!comment.getUserId().equals(userNo) && !comment.getUserId().equals("ADMIN")) {
            throw new AccessDeniedException("삭제할 권한이 없습니다.");
        }

        boolean hasChildren = commentRepository.existsByParentId(commentId);

        if(hasChildren) {
            comment.setCommentStatus(CommentStatus.DELETED);
            comment.setContent("");
            commentRepository.save(comment);
            return true;
        }
        Long parentId = comment.getParentId();
        commentRepository.delete(comment);
        if(parentId != null){
            commentRepository.findById(parentId).ifPresent(parent -> {
                if(parent.getCommentStatus() == CommentStatus.DELETED && !commentRepository.existsByParentId(parent.getId())) {
                    commentRepository.delete(parent);
                }
            });
        }
        return true;
    }
}
