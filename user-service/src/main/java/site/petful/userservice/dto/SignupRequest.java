package site.petful.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.petful.userservice.domain.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email; // userId 역할
    
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;
    
    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Size(max = 30, message = "이름은 30자를 초과할 수 없습니다.")
    private String name;
    
    @Size(max = 255, message = "닉네임은 255자를 초과할 수 없습니다.")
    private String nickname;
    
    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^[0-9-]+$", message = "올바른 전화번호 형식이 아닙니다.")
    private String phone;
    
    private Role userType;
    private LocalDate birthDate;
    
    @Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다.")
    private String description;
    
    @Size(max = 255, message = "도로명 주소는 255자를 초과할 수 없습니다.")
    private String roadAddress;
    
    @Size(max = 255, message = "상세 주소는 255자를 초과할 수 없습니다.")
    private String detailAddress;
    
    // 기존 필드들 (호환성을 위해 유지)
    @Size(max = 255, message = "주소는 255자를 초과할 수 없습니다.")
    private String address;
    
    @Size(max = 255, message = "상세 주소는 255자를 초과할 수 없습니다.")
    private String detailedAddress;
    
    private Integer birthYear;
    private Integer birthMonth;
    private Integer birthDay;
}
