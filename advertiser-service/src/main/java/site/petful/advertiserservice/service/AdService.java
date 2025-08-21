package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.advertiserservice.dto.advertisement.AdRequest;
import site.petful.advertiserservice.dto.advertisement.AdResponse;
import site.petful.advertiserservice.entity.Advertiser;
import site.petful.advertiserservice.entity.advertisement.AdStatus;
import site.petful.advertiserservice.entity.advertisement.Advertisement;
import site.petful.advertiserservice.repository.AdRepository;
import site.petful.advertiserservice.repository.AdvertiserRepository;

@Service
@RequiredArgsConstructor
public class AdService {

    private final AdvertiserRepository advertiserRepository;
    private final AdRepository adRepository;

    // 1. 광고(캠페인) 생성
    public AdResponse createAd(Long advertiserNo, AdRequest request) {

        Advertiser advertiser = advertiserRepository.findByAdvertiserNo(advertiserNo)
                .orElseThrow(() -> new RuntimeException("광고주를 찾을 수 없습니다."));

        Advertisement ad = new Advertisement();
        register(ad, request, advertiser);
        Advertisement saved = adRepository.save(ad);
        return AdResponse.from(saved);
    }

    private void register(Advertisement ad, AdRequest request, Advertiser advertiser) {
        ad.setTitle(request.getTitle());
        ad.setContent(request.getContent());
        ad.setObjective(request.getObjective());
        ad.setAnnounceStart(request.getAnnounceStart());
        ad.setAnnounceEnd(request.getAnnounceEnd());
        ad.setCampaignSelect(request.getCampaignSelect());
        ad.setCampaignStart(request.getCampaignStart());
        ad.setCampaignEnd(request.getCampaignEnd());
        ad.setApplicants(0);
        ad.setMembers(request.getMembers());
        ad.setAdStatus(ad.getAdStatus() == null ? AdStatus.PENDING : ad.getAdStatus());
        ad.setAdUrl(request.getAdUrl());
        ad.setAdvertiser(advertiser);
    }
}
