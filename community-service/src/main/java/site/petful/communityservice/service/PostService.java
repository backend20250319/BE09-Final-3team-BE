package site.petful.communityservice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        Post saved = postRepository.save(new Post(userNo, request.getTitle(),request.getContent(),request.getType()));
        return new PostDto(saved.getId(), saved.getUserId(), saved.getTitle(),
                saved.getContent(), saved.getCreatedAt(),saved.getType());
    }

    public Page<PostItem> getPosts(Pageable pageable, PostType type) {
        Page<Post> page = (type==null)
                ? postRepository.findByStatus(PostStatus.PUBLISHED,pageable)
                : postRepository.findByStatusAndType(PostStatus.PUBLISHED,pageable,type);
        List<Post> posts = page.getContent();
        Set<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long,UserBriefDto> userMap = Collections.emptyMap();
        if(!userIds.isEmpty()) {
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
        Map<Long, Long> countMap = Collections.emptyMap();
        if (!posts.isEmpty()) {
            List<Long> postIds = posts.stream().map(Post::getId).toList();

            var rows = commentRepository.findByPostIdInAndCommentStatus(postIds, CommentStatus.NORMAL);
            countMap = rows.stream().collect(
                    Collectors.groupingBy(CommentRepository.PostId::getPostId, Collectors.counting())
            );
        }
        Map<Long, Long> finalCountMap = countMap;
        Map<Long, UserBriefDto> finalUserMap = userMap;
        return page.map(p -> {
               UserBriefDto u =finalUserMap.get(p.getUserId());
               long cnt = finalCountMap.getOrDefault(p.getId(), 0L);
               return PostItem.from(p,cnt,u);
        });
    }

    public Page<PostItem> getMyPosts(Long userNo, Pageable pageable, PostType type) {
        Page<Post> page = (type==null)
                ? postRepository.findByUserIdAndStatus(userNo,PostStatus.PUBLISHED,pageable)
                : postRepository.findByUserIdAndStatusAndType(userNo,PostStatus.PUBLISHED,pageable,type);
        List<Post> posts = page.getContent();
        Map<Long, Long> countMap = Map.of();
        if (!posts.isEmpty()) {
            List<Long> postIds = posts.stream().map(Post::getId).toList();

            var rows = commentRepository.findByPostIdInAndCommentStatus(postIds, CommentStatus.NORMAL);
            countMap = rows.stream().collect(
                    Collectors.groupingBy(CommentRepository.PostId::getPostId, Collectors.counting())
            );
        }
        Map<Long, Long> finalCountMap = countMap;
        return page.map(p -> {
            long cnt = finalCountMap.getOrDefault(p.getId(), 0L);
            return PostItem.from(p, cnt);
        });
    }

    @Transactional(readOnly = true)
    public PostDetailDto getPostDetail(Long me, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 문자열 비교는 == 금지
        if ("DELETED".equals(post.getType().name())) {
            throw new RuntimeException("이미 삭제된 게시물입니다.");
        }

        // 댓글 포함하지 않음
        int commentCount = commentRepository.countByPostId(postId);

        return PostDetailDto.from(post, me, commentCount); // comments 없음
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
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("삭제할 게시물이 존재하지 않습니다.d"));
        if(!post.getUserId().equals(userNo)){
            throw new AccessDeniedException("게시물을 삭제할 권한이 없습니다.");
        }
        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
    }

}
