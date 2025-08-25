package site.petful.snsservice.instagram.comment.dto;

import lombok.Data;
import site.petful.snsservice.instagram.comment.entity.Sentiment;

@Data
public class CommentSearchCondition {

    private Boolean isDeleted;
    private Sentiment sentiment;
    private String keyword;
}
