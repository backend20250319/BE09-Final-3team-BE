package site.petful.campaignservice.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Pet {

    private Long petNo;
    private String name;
    private String type;
    private Integer age;
    private Character gender;
    private Boolean isPetstar;

}
