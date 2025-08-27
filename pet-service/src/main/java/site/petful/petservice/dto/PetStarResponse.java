package site.petful.petservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PetStarResponse {
    private Long snsProfileNo;
    private String petName;
    private String userName;
    private String userPhone;
    private String userEmail;
}
