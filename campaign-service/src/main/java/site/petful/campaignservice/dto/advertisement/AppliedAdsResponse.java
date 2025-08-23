package site.petful.campaignservice.dto.advertisement;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AppliedAdsResponse {

    private List<AdWithPetNosResponse> ads;

    public static AppliedAdsResponse from(List<AdWithPetNosResponse> adsWithPetNos) {
        AppliedAdsResponse response = new AppliedAdsResponse();
        response.setAds(adsWithPetNos);
        return response;
    }
}