package site.petful.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.petful.userservice.domain.Role;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    private String email; // userId 역할
    private String password;
    private String name;
    private String nickname;
    private String phone;
    private Role userType;
    private LocalDate birthDate;
    private String description;
    private String roadAddress;
    private String detailAddress;
    
    // 기존 필드들 (호환성을 위해 유지)
    private String address;
    private String detailedAddress;
    private Integer birthYear;
    private Integer birthMonth;
    private Integer birthDay;
}
