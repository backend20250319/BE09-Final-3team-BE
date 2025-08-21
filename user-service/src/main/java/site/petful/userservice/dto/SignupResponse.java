package site.petful.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// SignupResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    private Long userNo;      // 있으면
    private String email;
    private String name;
    private String message;
}
