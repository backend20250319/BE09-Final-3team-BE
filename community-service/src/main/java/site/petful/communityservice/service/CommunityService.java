package site.petful.communityservice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import site.petful.communityservice.dto.MyPostItem;
import site.petful.communityservice.dto.PostCreateRequest;
import site.petful.communityservice.dto.PostDto;
import site.petful.communityservice.entity.Post;
import site.petful.communityservice.entity.PostType;
import site.petful.communityservice.repository.CommunityRepository;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;

    public PostDto registNewPost(Long userNo, PostCreateRequest request) {
        Post saved = communityRepository.save(new Post(userNo, request.getTitle(),request.getContent(),request.getType()));
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
                ? communityRepository.findByUserId(userNo,pageable)
                : communityRepository.findByUserIdAndType(userNo,type,pageable);

        return posts.map(MyPostItem::from);
    }
}
