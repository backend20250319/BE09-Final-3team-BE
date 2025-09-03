package site.petful.userservice.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLogoutResponse {
    private String message;
    private Long adminId;
    private String adminType;
}

