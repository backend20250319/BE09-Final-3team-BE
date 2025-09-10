package site.petful.communityservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import site.petful.communityservice.client.UserClient;
import site.petful.communityservice.dto.*;
import site.petful.communityservice.entity.Comment;
import site.petful.communityservice.entity.CommentStatus;
import site.petful.communityservice.repository.CommentRepository;
import site.petful.communityservice.repository.PostRepository;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentService {

    @Autowired
    private CommentEventPublisher eventPublisher;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserClient userClient;

    @Transactional
    public CommentView createComment(Long userNo, CommentCreateRequest request) {
        // 401: 미인증
        if (userNo == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        // 400: 본문/필수값 검사
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청 본문이 비어있습니다.");
        }
        final Long postId = request.getPostId();
        if (postId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "postId는 필수입니다.");
        }
        final String content = Optional.ofNullable(request.getContent()).map(String::trim).orElse("");
        if (content.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "댓글 내용을 입력하세요.");
        }

        // 404: 게시글 존재/상태 확인 (가능하면 findByIdAndStatus(PUBLISHED) 사용)
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 게시물이 존재하지 않습니다.");
        }

        // 부모 댓글 유효성
        final Long parentId = request.getParentId();
        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글이 존재하지 않습니다."));
            if (!Objects.equals(parent.getPostId(), postId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "부모 댓글의 게시글이 일치하지 않습니다.");
            }
            if (parent.getParentId() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대댓글의 댓글은 허용하지 않습니다.");
            }
        }

        // 저장 (상태 명시)
        Comment saved = commentRepository.save(
                Comment.builder()
                        .userId(userNo)
                        .postId(postId)
                        .parentId(parentId)
                        .content(content)
                        .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))        // @PrePersist 있으면 생략 가능
                        .commentStatus(CommentStatus.NORMAL)    // 필수: NULL 방지
                        .build()
        );

        eventPublisher.publishCommentCreatedEvent(saved, postRepository.findById(request.getPostId()).get());
        // 작성자 정보 (없어도 저장은 되게, 표시용만 안전 처리)
        SimpleProfileResponse u = null;
        try {  var resp = userClient.getUserBrief(userNo); // ApiResponse<SimpleProfileResponse>
            if (resp != null) u = resp.getData(); } catch (Exception ignored) {}
        String nickname = (u != null && u.getNickname() != null && !u.getNickname().isBlank()) ? u.getNickname() : "익명";
        String avatar   = (u != null && u.getProfileImageUrl() != null && !u.getProfileImageUrl().isBlank())
                ? u.getProfileImageUrl()
                : "/user/avatar-placeholder.jpg";

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
                .commentStatus(saved.getCommentStatus())
                .children(java.util.List.of())
                .build();
    }

    @Transactional
    public Boolean deleteComment(Long userNo, Long commentId, String userType) throws AccessDeniedException {
        // 401: 미인증
        if (userNo == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // 404: 대상 없음
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "삭제할 댓글을 찾지 못했습니다.")
                );

        // 403: 권한 없음 (작성자 또는 관리자만)
        boolean isOwner = java.util.Objects.equals(comment.getUserId(), userNo);
        boolean isAdmin = userType != null && userType.equalsIgnoreCase("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제할 권한이 없습니다.");
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

            // Feign 배치 호출
            var resp = userClient.getUsersBrief(list);
            List<SimpleProfileResponse> profiles =
                    (resp != null && resp.getData() != null) ? resp.getData() : List.of();

            // SimpleProfileResponse -> UserBriefDto 매핑
            return profiles.stream()
                    .filter(p -> p.getId() != null)
                    .map(p -> new UserBriefDto(p.getId(), p.getNickname(), p.getProfileImageUrl()))
                    .collect(Collectors.toMap(UserBriefDto::getId, u -> u, (a, b) -> a));

        } catch (Exception e) {
            log.warn("fetchUsers failed: {}", e.getMessage());
            return Map.of(); // 실패 시 빈 맵 리턴
        }
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public void updateComment(Long userNo, Long commentId, CommentUpdateRequest request) {
        log.info("📝 [CommentService] 댓글 수정 요청: userId={}, commentId={}", userNo, commentId);

        // 404: 댓글 없음
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "수정할 댓글이 존재하지 않습니다.")
                );

        // 403: 권한 없음
        if (!Objects.equals(comment.getUserId(), userNo)) {
            throw new AccessDeniedException("댓글을 수정할 권한이 없습니다.");
        }

        // 댓글 내용 업데이트
        comment.setContent(request.getContent());
        comment.setUpdateAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        
        commentRepository.save(comment);
        
        log.info("✅ [CommentService] 댓글 수정 완료: commentId={}", commentId);
    }

}
