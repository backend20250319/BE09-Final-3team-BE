package site.petful.userservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoResponse {
    private Long userNo;
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private String role;
}
