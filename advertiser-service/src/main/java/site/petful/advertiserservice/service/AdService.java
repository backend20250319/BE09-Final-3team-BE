package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.advertiserservice.dto.advertisement.AdRequest;
import site.petful.advertiserservice.dto.advertisement.AdResponse;
import site.petful.advertiserservice.entity.Advertiser;
import site.petful.advertiserservice.entity.advertisement.*;
import site.petful.advertiserservice.repository.AdRepository;
import site.petful.advertiserservice.repository.AdvertiserRepository;

import java.util.List;
import java.util.stream.Collectors;

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

        List<Mission> missions = request.getMission().stream()
                .map(mr -> {
                    Mission m = new Mission();
                    m.setContent(mr.getContent());
                    m.setAdvertisement(ad);
                    return m;
                }).collect(Collectors.toList());
        ad.setMission(missions);

        List<Keyword> keywords = request.getKeyword().stream()
                .map(kr -> {
                    Keyword k = new Keyword();
                    k.setContent(kr.getContent());
                    k.setAdvertisement(ad);
                    return k;
                }).collect(Collectors.toList());
        ad.setKeyword(keywords);

        List<Requirement> reqs = request.getRequirement().stream()
                .map(rr -> {
                    Requirement r = new Requirement();
                    r.setContent(rr.getContent());
                    r.setAdvertisement(ad);
                    return r;
                }).collect(Collectors.toList());
        ad.setRequirement(reqs);
    }
}
