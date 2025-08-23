package site.petful.campaignservice.dto.advertisement;

import lombok.Getter;
import lombok.Setter;
import site.petful.campaignservice.entity.Advertiser;

@Getter
@Setter
public class AdvertiserResponse {

    private String name;
    private String phone;
    private String website;
    private String email;
    private String description;

    public static AdvertiserResponse from(Advertiser advertiser) {
        AdvertiserResponse res = new AdvertiserResponse();
        res.setName(advertiser.getName());
        res.setPhone(advertiser.getPhone());
        res.setWebsite(advertiser.getWebsite());
        res.setEmail(advertiser.getEmail());
        res.setDescription(advertiser.getDescription());

        return res;
    }
}

