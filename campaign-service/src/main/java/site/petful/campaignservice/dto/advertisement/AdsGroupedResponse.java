package site.petful.campaignservice.dto.advertisement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdsGroupedResponse {

    private List<AdResponse> recruitingAds;
    private List<AdResponse> endedAds;
}
