package site.petful.advertiserservice.dto.advertiser;

import lombok.*;

@Getter
public class AdvertiserRequest {

    private String name;
    private String phone;
    private String website;
    private String email;
    private String description;

}