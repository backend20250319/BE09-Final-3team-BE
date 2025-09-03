package site.petful.campaignservice.dto.advertisement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdvertiserResponse {

    private Long advertiserNo;
    private String name;
    private String phone;
    private String website;
    private String email;
    private String description;
}

