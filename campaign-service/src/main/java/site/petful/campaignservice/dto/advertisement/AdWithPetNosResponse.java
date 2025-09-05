package site.petful.campaignservice.dto.advertisement;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdWithPetNosResponse {

    private AdResponse advertisement;
    private List<Long> appliedPetNos;

    public static AdWithPetNosResponse from(AdResponse adResponse, List<Long> petNos) {
        AdWithPetNosResponse res = new AdWithPetNosResponse();
        res.advertisement = adResponse;
        res.appliedPetNos = petNos;
        return res;
    }
}
