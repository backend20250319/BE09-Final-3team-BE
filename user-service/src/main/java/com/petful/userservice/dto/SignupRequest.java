package com.petful.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String phone;
    private String address;
    private String detailedAddress;
    private Integer birthYear;
    private Integer birthMonth;
    private Integer birthDay;
}
