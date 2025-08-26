package site.petful.snsservice.instagram.comment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Sentiment {
    POSITIVEO("긍정"),
    NEGATIVE("부정"),
    NEUTRAL("중립");


    private final String description;
}
