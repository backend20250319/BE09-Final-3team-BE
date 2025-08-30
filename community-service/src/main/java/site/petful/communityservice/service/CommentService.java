package site.petful.communityservice.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.communityservice.client.UserClient;
import site.petful.communityservice.dto.*;
import site.petful.communityservice.entity.Comment;
import site.petful.communityservice.entity.CommentStatus;
import site.petful.communityservice.repository.CommentRepository;
import site.petful.communityservice.repository.PostRepository;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserClient userClient;

    @Transactional
    public CommentView createComment(Long userNo, CommentCreateRequest request) {
        UserBriefDto user = userClient.getUserBrief(userNo);

        Long postId = request.getPostId();
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("해당 게시물이 존재하지 않습니다.");
        }

        Long parentId = request.getParentId();
        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
            if (!parent.getPostId().equals(postId)) {
                throw new IllegalArgumentException("부모 댓글의 게시글이 일치하지 않습니다.");
            }
            if (parent.getParentId() != null) {
                throw new IllegalArgumentException("대댓글의 댓글은 허용하지 않습니다.");
            }
        }

        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (content.isEmpty()) {
            throw new IllegalArgumentException("댓글 내용을 입력하세요.");
        }

        Comment saved = commentRepository.save(
                Comment.builder()
                        .userId(userNo)
                        .postId(postId)
                        .parentId(parentId)
                        .content(content)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        String nickname = user.getName();
        String avatar = (user.getPorfileUrl() != null && !user.getPorfileUrl().isBlank())
                ? user.getPorfileUrl() : "/default-avatar.png";

        AuthorDto author = AuthorDto.builder()
                .id(userNo)
                .nickname(nickname)
                .profileImageUrl(avatar)
                .build();

        return CommentView.builder()
                .id(saved.getId())
                .parentId(saved.getParentId())
                .userId(saved.getUserId())
                .author(author)
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .children(List.of())
                .build();
    }

    @Transactional
    public Boolean deleteComment(Long userNo, Long commentId, String userType) throws AccessDeniedException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("삭제할 댓글을 찾지 못했습니다."));
        if (!comment.getUserId().equals(userNo) && !"ADMIN".equals(userType)) {
            throw new AccessDeniedException("삭제할 권한이 없습니다.");
        }

        boolean hasChildren = commentRepository.existsByParentId(commentId);

        if (hasChildren) {
            comment.setCommentStatus(CommentStatus.DELETED);
            comment.setContent("");
            commentRepository.save(comment);
            return true;
        }

        Long parentId = comment.getParentId();
        commentRepository.delete(comment);
        if (parentId != null) {
            commentRepository.findById(parentId).ifPresent(parent -> {
                if (parent.getCommentStatus() == CommentStatus.DELETED &&
                        !commentRepository.existsByParentId(parent.getId())) {
                    commentRepository.delete(parent);
                }
            });
        }
        return true;
    }

    public CommentPageDto listComments(Long postId, Pageable pageable) {
        Page<Comment> roots = commentRepository.findByPostIdAndParentIdIsNull(postId, pageable);

        // 대댓글
        List<Long> rootIds = roots.getContent().stream().map(Comment::getId).toList();
        List<Comment> children = roots.isEmpty()
                ? List.of()
                : commentRepository.findByPostIdAndParentIdIn(postId, rootIds);

        // 유저 정보 조회
        Set<Long> userIds = new HashSet<>();
        roots.forEach(c -> userIds.add(c.getUserId()));
        children.forEach(c -> userIds.add(c.getUserId()));

        Map<Long, UserBriefDto> userMap = fetchUsers(userIds);

        // parentId -> 자식 매핑
        Map<Long, List<Comment>> childByParent = children.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        // DTO 변환
        List<CommentView> content = roots.getContent().stream()
                .map(root -> toView(root, childByParent.getOrDefault(root.getId(), List.of()), userMap))
                .toList();

        return CommentPageDto.builder()
                .content(content)
                .page(roots.getNumber())
                .size(roots.getSize())
                .totalElements(roots.getTotalElements())
                .last(roots.isLast())
                .build();
    }

    private CommentView toView(Comment c, List<Comment> kids, Map<Long, UserBriefDto> umap) {
        AuthorDto author = toAuthor(umap.get(c.getUserId()), c.getUserId());

        List<CommentView> childViews = kids.stream().map(k -> CommentView.builder()
                .id(k.getId())
                .parentId(k.getParentId())
                .userId(k.getUserId())
                .author(toAuthor(umap.get(k.getUserId()), k.getUserId()))
                .content(k.getContent())
                .createdAt(k.getCreatedAt())
                .commentStatus(k.getCommentStatus())
                .children(List.of())
                .build()
        ).toList();

        return CommentView.builder()
                .id(c.getId())
                .parentId(c.getParentId())
                .userId(c.getUserId())
                .author(author)
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .commentStatus(c.getCommentStatus())
                .children(childViews)
                .build();
    }

    private AuthorDto toAuthor(UserBriefDto u, Long userId) {
        if (u == null) {
            return AuthorDto.builder()
                    .id(userId)
                    .nickname("익명")
                    .profileImageUrl(null)
                    .build();
        }
        return AuthorDto.from(u);
    }

    private Map<Long, UserBriefDto> fetchUsers(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        try {
            List<Long> list = new ArrayList<>(ids);
            List<UserBriefDto> brief = userClient.getUsersBrief(list); // Feign 호출
            return brief.stream()
                    .filter(u -> u.getId() != null)
                    .collect(Collectors.toMap(UserBriefDto::getId, u -> u, (a, b) -> a));
        } catch (Exception e) {
            return Map.of(); // 실패 시 폴백
        }
    }
}
