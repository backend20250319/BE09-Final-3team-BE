package site.petful.advertiserservice.dto.advertisement;

import lombok.Getter;
import site.petful.advertiserservice.entity.advertisement.AdStatus;

@Getter
public class AdRequestByAdmin {

    private AdStatus adStatus;
    private String reason;
}
