package site.petful.communityservice.service;



import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import site.petful.communityservice.client.UserClient;
import site.petful.communityservice.dto.*;
import site.petful.communityservice.entity.*;
import site.petful.communityservice.repository.CommentRepository;
import site.petful.communityservice.repository.PostRepository;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserClient userClient;

    @Transactional
    public PostDto registNewPost(Long userNo, PostCreateRequest request) {
        // 401: 인증 필요
        if (userNo == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // 400: 본문/필수 필드 검사
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청 본문이 비어있습니다.");
        }

        final String title   = request.getTitle()   == null ? "" : request.getTitle().trim();
        final String content = request.getContent() == null ? "" : request.getContent().trim();
        final PostType type  = request.getType(); // enum

        if (title.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "제목을 입력하세요.");
        }
        if (content.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "내용을 입력하세요.");
        }
        if (type == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "게시글 유형(type)은 필수입니다.");
        }

        Post saved = postRepository.save(new Post(userNo, request.getTitle(),request.getContent(),request.getType()));
        return new PostDto(saved.getId(), saved.getUserId(), saved.getTitle(),
                saved.getContent(), saved.getCreatedAt(),saved.getType());
    }

    public Page<PostItem> getPosts(Pageable pageable, PostType type) {
        Page<Post> page = (type == null)
                ? postRepository.findByStatus(PostStatus.PUBLISHED, pageable)
                : postRepository.findByStatusAndType(PostStatus.PUBLISHED, pageable, type);

        List<Post> posts = page.getContent();

        Set<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, UserBriefDto> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            try {
                List<UserBriefDto> users = userClient.getUsersBrief(new ArrayList<>(userIds));
                userMap = users.stream().collect(Collectors.toMap(UserBriefDto::getId, Function.identity()));
            } catch (Exception batchFail) {
                Map<Long, UserBriefDto> tmp = new HashMap<>();
                for (Long id : userIds) {
                    try { tmp.put(id, userClient.getUserBrief(id)); }
                    catch (Exception ignore) { tmp.put(id, null); }
                }
                userMap = tmp;
            }
        }

        Map<Long, UserBriefDto> finalUserMap = userMap;

        return page.map(p -> {
            UserBriefDto u = finalUserMap.get(p.getUserId());
            int cnt = commentRepository.countByPostId(p.getId());
            return PostItem.from(p, cnt, u);
        });
    }

    public Page<PostItem> getMyPosts(Long userNo, Pageable pageable, PostType type) {
        Page<Post> page = (type == null)
                ? postRepository.findByUserIdAndStatus(userNo, PostStatus.PUBLISHED, pageable)
                : postRepository.findByUserIdAndStatusAndType(userNo, PostStatus.PUBLISHED, pageable, type);
        UserBriefDto u;
        try {
            u = userClient.getUserBrief(userNo);
        } catch (Exception e) {
            u = null;
        }
        if (u == null || u.getId() == null) {
            u = UserBriefDto.builder()
                    .id(userNo)
                    .name("익명")
                    .profileUrl(null)
                    .build();
        }
        final UserBriefDto tmp = u;
        return page.map(p -> {
            int cnt = commentRepository.countByPostId(p.getId());
            return PostItem.from(p, cnt, tmp);
        });
    }

    @Transactional(readOnly = true)
    public PostDetailDto getPostDetail(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 문자열 비교는 == 금지
        if ("DELETED".equals(post.getType().name())) {
            throw new RuntimeException("이미 삭제된 게시물입니다.");
        }
        UserBriefDto u = userClient.getUserBrief(userId);

        int commentCount = commentRepository.countByPostId(postId);

        return PostDetailDto.from(post, commentCount, u);
    }

    private List<CommentNode> buildTree(List<Comment> comments) {
        Map<Long, CommentNode> nodeMap = new HashMap<>();
        List<CommentNode> roots = new ArrayList<>();

        for(Comment c: comments){
            nodeMap.put(c.getId(), CommentNode.of(c));
        }
        for(Comment c : comments){
            CommentNode node = nodeMap.get(c.getId());
            if(c.getParentId() == null){
                roots.add(node);
            }else{
                CommentNode parent = nodeMap.get(c.getParentId());
                if(parent != null){
                    parent.getChildren().add(node);
                }else{
                    roots.add(node);
                }
            }
        }
        return roots;
    }

    @Transactional
    public void deletePost(Long userNo, Long postId) throws AccessDeniedException {
        if (userNo == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // 404: 게시글 없음
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "삭제할 게시물이 존재하지 않습니다.")
                );

        // 403: 권한 없음
        if (!Objects.equals(post.getUserId(), userNo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시물을 삭제할 권한이 없습니다.");
        }

        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
    }

}
