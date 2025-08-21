package site.petful.communityservice.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.petful.communityservice.entity.PostType;

@Getter
@RequiredArgsConstructor
public class PostCreateRequest {
    private final String title;

    private final String content;

    private PostType type;
}
