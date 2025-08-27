package site.petful.userservice.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class PetStarResponse {
    private Long snsProfileNo;
//    private String snsName;
    private String petName;
    private String userName;
    private String userPhone;
    private String userEmail;

}
