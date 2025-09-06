package site.petful.petservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstagramProfileInfo {
    private Long id;
    private String username;
    private String name;
    private String profilePictureUrl;
    private Long followersCount;
    private Long followsCount;
    private Long mediaCount;
    private Boolean autoDelete;
}
