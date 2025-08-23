package site.petful.campaignservice.dto.campaign;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PetResponse {
    private Long petNo;
    private String name;
    private String type;
    private Integer age;
    private Character gender;
    private Boolean isPetstar;
}
