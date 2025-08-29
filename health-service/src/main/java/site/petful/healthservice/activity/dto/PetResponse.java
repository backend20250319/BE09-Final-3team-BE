package site.petful.healthservice.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PetResponse {
    private Long petNo;
    private Long userNo;
    private String name;
    private String type;
    private String imageUrl;
    private Integer age;
    private String gender;
    private Double weight;
    private Boolean isPetStar;
    private Long snsProfileNo;
    private String petStarStatus;
}
