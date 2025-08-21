package site.petful.advertiserservice.dto.advertisement;

import lombok.Getter;
import site.petful.advertiserservice.entity.advertisement.AdStatus;

import java.time.LocalDate;

@Getter
public class AdRequest {

    private String title;
    private String content;
    private String objective;
    private LocalDate announceStart;
    private LocalDate announceEnd;
    private LocalDate campaignSelect;
    private LocalDate campaignStart;
    private LocalDate campaignEnd;
    private Integer members;
    private String adUrl;
}
