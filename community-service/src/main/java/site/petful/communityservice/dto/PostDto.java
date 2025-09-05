package site.petful.communityservice.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.petful.communityservice.entity.PostType;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public final class PostDto {
    private final Long id;
    private final Long userId;
    private final String title;
    private final String content;
    private final LocalDateTime createdAt;
    private final PostType type;
}
