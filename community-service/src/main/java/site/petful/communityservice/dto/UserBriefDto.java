package site.petful.communityservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class UserBriefDto {
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("nickname")
    private String nickname;
    
    @JsonProperty("profileImageUrl")
    private String profileImageUrl;
}
