package site.petful.advertiserservice.dto.advertisement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.petful.advertiserservice.entity.advertisement.Advertisement;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdsResponse {
    private List<AdResponse> ads;

    public static AdsResponse from(List<Advertisement> adList) {
        List<AdResponse> adResponses = adList.stream()
                .map(AdResponse::from)
                .collect(Collectors.toList());
        return new AdsResponse(adResponses);
    }
}
