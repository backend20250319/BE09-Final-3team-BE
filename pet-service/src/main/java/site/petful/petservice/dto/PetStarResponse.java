package site.petful.petservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PetStarResponse {
    private Long petNo;
    private Long snsId;
    private String petName;
    private Long age;
    private String userName;
    private String userPhone;
    private String userEmail;
    private String petType;
    private String petGender;
}
