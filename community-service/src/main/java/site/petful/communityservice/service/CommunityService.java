package site.petful.communityservice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.communityservice.dto.PostCreateRequest;
import site.petful.communityservice.dto.PostDto;
import site.petful.communityservice.entity.Post;
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
}
