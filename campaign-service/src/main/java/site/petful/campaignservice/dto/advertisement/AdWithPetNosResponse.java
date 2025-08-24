package site.petful.campaignservice.dto.advertisement;

import lombok.Getter;
import lombok.Setter;
import site.petful.campaignservice.entity.advertisement.Advertisement;

import java.util.List;

@Getter
@Setter
public class AdWithPetNosResponse {

    private AdResponse advertisement;
    private List<Long> appliedPetNos;

    public static AdWithPetNosResponse from(Advertisement ad, List<Long> petNos) {
        AdWithPetNosResponse res = new AdWithPetNosResponse();
        res.advertisement = AdResponse.from(ad);
        res.appliedPetNos = petNos;
        return res;
    }
}
