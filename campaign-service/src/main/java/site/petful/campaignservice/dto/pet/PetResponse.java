package site.petful.campaignservice.dto.pet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import site.petful.campaignservice.entity.PetStarStatus;

@Getter
@Setter
@AllArgsConstructor
public class PetResponse {
    private Long petNo;
    private Long userNo;
    private String name;
    private String type;
    private String imageUrl;
    private Long age;
    private String gender;
    private Float weight;
    private Boolean isPetStar;
    private Long snsId;
    private String snsUsername;
    private PetStarStatus petStarStatus;
}
