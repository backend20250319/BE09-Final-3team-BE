package site.petful.communityservice.service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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

@Slf4j
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

        Map<Long, UserBriefDto> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            try {
                // ★ 현재 요청 컨텍스트 확인
                var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    var req = attrs.getRequest();
                    String auth = req.getHeader("Authorization");
                    String userNo = req.getHeader("X-User-No");
                    log.info("Current request context - Authorization: {}, X-User-No: {}", 
                            auth != null ? auth.substring(0, Math.min(20, auth.length())) + "..." : "null", userNo);
                } else {
                    log.warn("No request context available for FeignClient call");
                }
                
                // ★ 배치 호출: ApiResponse<List<SimpleProfileResponse>>
                log.info("Calling userClient.getUsersBrief with userIds: {}", userIds);
                var resp = userClient.getUsersBrief(new ArrayList<>(userIds));
                log.info("UserClient batch response: {}", resp);
                
                if (resp == null) {
                    log.warn("UserClient batch response is null");
                    throw new RuntimeException("UserClient response is null");
                }
                
                List<SimpleProfileResponse> list =
                        (resp.getData() != null) ? resp.getData() : List.of();
                log.info("UserClient batch list size: {}", list.size());
                
                // 각 응답 항목 로깅
                for (SimpleProfileResponse profile : list) {
                    log.info("Batch profile: id={}, nickname={}, profileImageUrl={}, email={}, phone={}", 
                            profile.getId(), profile.getNickname(), profile.getProfileImageUrl(), 
                            profile.getEmail(), profile.getPhone());
                }

                userMap = list.stream()
                        .map(this::toBrief)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(UserBriefDto::getId, u -> u, (a, b) -> a));

                log.info("Successfully fetched {} user profiles via batch call", userMap.size());

            } catch (Exception batchFail) {
                log.error("getUsersBrief batch failed: {}", batchFail.getMessage(), batchFail);
                // 배치 호출 실패 시 단건 호출로 fallback
                for (Long id : userIds) {
                    try {
                        // ★ 단건 호출: ApiResponse<SimpleProfileResponse>
                        var single = userClient.getUserBrief(id);
                        SimpleProfileResponse p = (single != null) ? single.getData() : null;
                        UserBriefDto userBrief = toBrief(p);
                        if (userBrief != null) {
                            userMap.put(id, userBrief);
                        } else {
                            // 사용자 정보를 가져올 수 없는 경우 기본값 설정
                            userMap.put(id, UserBriefDto.builder()
                                    .id(id)
                                    .nickname("익명")
                                    .profileImageUrl(null)
                                    .build());
                        }
                    } catch (Exception e) {
                        log.warn("getUserBrief({}) failed: {}", id, e.getMessage());
                        // 예외 발생 시에도 기본값 설정
                        userMap.put(id, UserBriefDto.builder()
                                .id(id)
                                .nickname("익명")
                                .profileImageUrl(null)
                                .build());
                    }
                }
            }
        }

        Map<Long, UserBriefDto> finalUserMap = userMap;

        return page.map(p -> {
            UserBriefDto u = finalUserMap.get(p.getUserId());
            // userMap에 없는 경우 기본값 생성
            if (u == null) {
                log.warn("User not found in userMap for userId: {}, creating default user info", p.getUserId());
                u = UserBriefDto.builder()
                        .id(p.getUserId())
                        .nickname("사용자" + p.getUserId()) // 기본값을 "익명" 대신 "사용자{ID}"로 변경
                        .profileImageUrl(null)
                        .build();
            }
            int cnt = commentRepository.countByPostId(p.getId());
            return PostItem.from(p, cnt, u);
        });
    }


    private  UserBriefDto toBrief(SimpleProfileResponse p) {
        if (p == null || p.getId() == null) {
            log.warn("SimpleProfileResponse is null or has null id");
            return null;
        }
        
                log.info("Converting SimpleProfileResponse: id={}, nickname={}, profileImageUrl={}, email={}, phone={}",
                        p.getId(), p.getNickname(), p.getProfileImageUrl(), p.getEmail(), p.getPhone());
        
        // nickname이 null이거나 빈 문자열인 경우 기본값 설정
        String nickname = p.getNickname();
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = "익명";
            log.warn("Nickname is null or empty for user {}, using default: {}", p.getId(), nickname);
        }
        
        UserBriefDto result = new UserBriefDto(
                p.getId(),
                nickname,
                p.getProfileImageUrl()
        );
        
        log.info("Converted to UserBriefDto: id={}, nickname={}, profileImageUrl={}", 
                 result.getId(), result.getNickname(), result.getProfileImageUrl());
        
        return result;
    }

    public Page<PostItem> getMyPosts(Long userNo, Pageable pageable, PostType type) {
        Page<Post> page = (type == null)
                ? postRepository.findByUserIdAndStatus(userNo, PostStatus.PUBLISHED, pageable)
                : postRepository.findByUserIdAndStatusAndType(userNo, PostStatus.PUBLISHED, pageable, type);
        UserBriefDto brief = null;
        try {
            var resp = userClient.getUserBrief(userNo);                 // ApiResponse<SimpleProfileResponse>
            SimpleProfileResponse payload = (resp != null ? resp.getData() : null);
            if (payload != null && payload.getId() != null) {
                brief = new UserBriefDto(payload.getId(), payload.getNickname(), payload.getProfileImageUrl());
            }
        } catch (Exception ignored) { /* 로그 필요시 추가 */ }

        if (brief == null || brief.getId() == null) {
            brief = UserBriefDto.builder()
                    .id(userNo)
                    .nickname("익명")
                    .profileImageUrl(null)
                    .build();
        }
        final UserBriefDto tmp = brief;
        return page.map(p -> {
            int cnt = commentRepository.countByPostId(p.getId());
            return PostItem.from(p, cnt, tmp);
        });
    }

    @Transactional(readOnly = true)
    public PostDetailDto getPostDetail(Long currentUserId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 문자열 비교는 == 금지
        if ("DELETED".equals(post.getType().name())) {
            throw new RuntimeException("이미 삭제된 게시물입니다.");
        }
        UserBriefDto author = null;
        try {
            var resp = userClient.getUserBrief(post.getUserId()); // ApiResponse<SimpleProfileResponse>
            log.info("UserClient response for user {}: {}", post.getUserId(), resp);
            SimpleProfileResponse payload = (resp != null ? resp.getData() : null);
            log.info("UserClient payload for user {}: {}", post.getUserId(), payload);
            author = toBrief(payload);
        } catch (Exception e) {
            log.warn("Failed to get user brief for post {}: {}", postId, e.getMessage());
            author = UserBriefDto.builder()
                    .id(post.getUserId())
                    .nickname("익명")
                    .profileImageUrl(null)
                    .build();
        }

        // author가 null인 경우 기본값 설정
        if (author == null) {
            author = UserBriefDto.builder()
                    .id(post.getUserId())
                    .nickname("익명")
                    .profileImageUrl(null)
                    .build();
        }

        // 현재 로그인한 사용자와 게시글 작성자 비교
        boolean isMine = currentUserId != null && post.getUserId() != null 
                        && currentUserId.equals(post.getUserId());
        log.info("isMine calculation - currentUserId: {}, post.userId: {}, isMine: {}", 
                currentUserId, post.getUserId(), isMine);

        int commentCount = commentRepository.countByPostId(postId);

        return PostDetailDto.from(post, commentCount, author, isMine);
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

    /**
     * 게시글 수정
     */
    @Transactional
    public void updatePost(Long userNo, Long postId, PostUpdateRequest request) {
        log.info("📝 [PostService] 게시글 수정 요청: userId={}, postId={}", userNo, postId);

        // 404: 게시글 없음
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "수정할 게시물이 존재하지 않습니다.")
                );

        // 403: 권한 없음
        if (!Objects.equals(post.getUserId(), userNo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시물을 수정할 권한이 없습니다.");
        }

        // 게시글 정보 업데이트
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setType(request.getType());
        
        postRepository.save(post);
        
        log.info("✅ [PostService] 게시글 수정 완료: postId={}", postId);
    }

}
