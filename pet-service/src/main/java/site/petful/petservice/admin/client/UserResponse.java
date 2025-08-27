package site.petful.petservice.admin.client;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
}