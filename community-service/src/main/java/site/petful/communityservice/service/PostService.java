package site.petful.communityservice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import site.petful.communityservice.dto.MyPostItem;
import site.petful.communityservice.dto.PostCreateRequest;
import site.petful.communityservice.dto.PostDetail;
import site.petful.communityservice.dto.PostDto;
import site.petful.communityservice.entity.Post;
import site.petful.communityservice.entity.PostType;
import site.petful.communityservice.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

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

        return posts.map(MyPostItem::from);
    }

    public PostDetail postDetail(Long userNo, Long postId) {
        Post detail = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if(detail.getType().name() == "DELETED"){
            throw new RuntimeException("이미 삭제된 게시물입니다.");
        }
        int commentCount = 0;
        return PostDetail.from(detail,userNo,commentCount);
    }
}
