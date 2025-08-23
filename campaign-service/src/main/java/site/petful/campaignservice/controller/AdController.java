package site.petful.campaignservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.campaignservice.service.AdService;

@RestController
@RequestMapping("/campaign")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }

    // 1. adStatus별 (모집중/종료된) 광고 조회


}
