package site.petful.communityservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class UserBriefDto {
    private Long id;
    private String name;
    private String porfileUrl;

}
