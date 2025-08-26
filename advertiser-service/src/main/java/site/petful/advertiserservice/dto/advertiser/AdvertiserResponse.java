package site.petful.advertiserservice.dto.advertiser;

import lombok.*;
import site.petful.advertiserservice.entity.Advertiser;

@Getter
@Setter
public class AdvertiserResponse {

    private String name;
    private String phone;
    private String website;
    private String email;
    private String description;
    private String reason;

    public static AdvertiserResponse from(Advertiser advertiser) {
        AdvertiserResponse res = new AdvertiserResponse();
        res.setName(advertiser.getName());
        res.setPhone(advertiser.getPhone());
        res.setWebsite(advertiser.getWebsite());
        res.setEmail(advertiser.getEmail());
        res.setDescription(advertiser.getDescription());
        res.setReason(advertiser.getReason());

        return res;
    }
}