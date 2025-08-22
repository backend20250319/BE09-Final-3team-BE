package site.petful.communityservice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.communityservice.dto.*;
import site.petful.communityservice.entity.Comment;
import site.petful.communityservice.entity.Post;
import site.petful.communityservice.entity.PostType;
import site.petful.communityservice.repository.CommentRepository;
import site.petful.communityservice.repository.PostRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public PostDto registNewPost(Long userNo, PostCreateRequest request) {
        Post saved = postRepository.save(new Post(userNo, request.getTitle(),request.getContent(),request.getType()));
        return new PostDto(saved.getId(), saved.getUserId(), saved.getTitle(),
                saved.getContent(), saved.getCreatedAt(),saved.getType());
    }

    public Page<MyPostItem> getMyPosts(Long userNo, Integer page, Integer size, PostType type) {
        Pageable pageable = PageRequest.of(
                page == null ? 0 : page,
                size == null ? 20 : size,
                Sort.by(Sort.Direction.DESC,"createdAt")
        );

        Page<Post> posts = (type==null)
                ? postRepository.findByUserId(userNo,pageable)
                : postRepository.findByUserIdAndType(userNo,type,pageable);

        return posts.map(p -> {
                int cnt = commentRepository.countByPostId(p.getId());
                return MyPostItem.from(p, cnt);
                }
              );
    }

    public PostDetail getPostDetail(Long userNo, Long postId) {
        Post detail = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if(detail.getType().name() == "DELETED"){
            throw new RuntimeException("이미 삭제된 게시물입니다.");
        }
        List<Comment> all = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        List<CommentNode> tree = buildTree(all);

        int commentCount = all.size();
        return PostDetail.from(detail,userNo,commentCount).withComments(tree);
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
    public Void deletePost(Long userNo, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("삭제할 게시물이 존재하지 않습니다.d"));
        postRepository.delete(post);
        return null;
    }
}
