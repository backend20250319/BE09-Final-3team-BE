package site.petful.advertiserservice.dto.advertiser;

import lombok.*;
import site.petful.advertiserservice.entity.Advertiser;
import site.petful.advertiserservice.signup.entity.AdvertiserSignup;

@Getter
@Setter
public class AdvertiserResponse {

    private String name;
    private String phone;
    private String website;
    private String email;
    private String description;
    private String reason;

    public static AdvertiserResponse from(AdvertiserSignup advertiser) {
        AdvertiserResponse res = new AdvertiserResponse();
        res.setName(advertiser.getName());
        res.setPhone(advertiser.getPhone());
        res.setWebsite(null); // AdvertiserSignup에는 website 필드가 없음
        res.setEmail(advertiser.getUserId()); // userId를 email로 사용
        res.setDescription(advertiser.getDescription());
        res.setReason(advertiser.getReason());

        return res;
    }

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