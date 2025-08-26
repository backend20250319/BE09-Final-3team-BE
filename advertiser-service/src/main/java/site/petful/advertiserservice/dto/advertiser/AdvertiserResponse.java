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
    res.setName(advertiser.getCompanyName());
    res.setPhone(advertiser.getPhoneNumber());
    res.setWebsite(null); // Advertiser 엔티티에 website 필드가 없음
    res.setEmail(advertiser.getEmail());
    res.setDescription(null); // Advertiser 엔티티에 description 필드가 없음
    res.setReason(null); // Advertiser 엔티티에 reason 필드가 없음

    return res;
}
}