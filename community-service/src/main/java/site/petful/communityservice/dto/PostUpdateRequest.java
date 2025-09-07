package site.petful.communityservice.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.petful.communityservice.entity.PostType;

@Getter
@RequiredArgsConstructor
public class PostUpdateRequest {
    private final String title;
    private final String content;
    private final PostType type;
}
