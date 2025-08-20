package site.petful.userservice.dto;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}
