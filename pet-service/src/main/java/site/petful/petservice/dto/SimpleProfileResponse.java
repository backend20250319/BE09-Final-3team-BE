package site.petful.petservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleProfileResponse {
    private Long id;
    private String nickname;
    private String profileImageUrl;
    private String emil;
    private String phone;

}
