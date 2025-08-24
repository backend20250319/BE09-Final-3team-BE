package site.petful.snsservice.instagram.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import site.petful.snsservice.instagram.client.dto.InstagramMediaResponseDto.Paging;
import site.petful.snsservice.instagram.comment.dto.InstagramCommentDto;

/**
 * API의 최상위 응답 구조를 나타내는 DTO 클래스입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class InstagramCommentResponseDto {

    @JsonProperty("data")
    private List<InstagramCommentDto> data;

    @JsonProperty("paging")
    private Paging paging;


}
