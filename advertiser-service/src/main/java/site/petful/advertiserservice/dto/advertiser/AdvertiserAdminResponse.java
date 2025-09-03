package site.petful.advertiserservice.dto.advertiser;

import lombok.*;
import site.petful.advertiserservice.entity.advertiser.Advertiser;

@Getter
@Setter
public class AdvertiserAdminResponse {

    private Long advertiserNo;
    private String name;
    private String phone;
    private String website;
    private String email;
    private String description;
    private String reason;
    private String businessNumber;

    public static AdvertiserAdminResponse from(Advertiser advertiser) {
        AdvertiserAdminResponse res = new AdvertiserAdminResponse();
        res.setAdvertiserNo(advertiser.getAdvertiserNo());
        res.setName(advertiser.getName());
        res.setPhone(advertiser.getPhone());
        res.setWebsite(advertiser.getWebsite());
        res.setEmail(advertiser.getEmail());
        res.setDescription(advertiser.getDescription());
        res.setReason(advertiser.getReason());
        res.setBusinessNumber(advertiser.getBusinessNumber());

        return res;
    }
}