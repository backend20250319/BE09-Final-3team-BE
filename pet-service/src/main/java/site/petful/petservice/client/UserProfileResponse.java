package site.petful.petservice.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long userNo;
    private String email;
    private String name;
    private String phone;
    private String nickname;
}
