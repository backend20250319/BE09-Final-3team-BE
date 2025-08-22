package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.dto.advertisement.*;
import site.petful.advertiserservice.entity.Advertiser;
import site.petful.advertiserservice.entity.advertisement.*;
import site.petful.advertiserservice.repository.AdRepository;
import site.petful.advertiserservice.repository.AdvertiserRepository;

import java.util.ArrayList;
import java.util.Collections;
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
                .orElseThrow(() -> new RuntimeException(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage()));

        Advertisement ad = new Advertisement();
        register(ad, request, advertiser);
        Advertisement saved = adRepository.save(ad);
        return AdResponse.from(saved);
    }

    // 2-1. 광고(캠페인) 단일 조회
    public AdResponse getAd(Long adNo) {

        Advertisement ad = adRepository.findByAdNo(adNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.AD_NOT_FOUND.getDefaultMessage()));
        return AdResponse.from(ad);
    }

    // 2-2. 광고(캠페인) 전체 조회

    // 2-3. 광고주별 광고(캠페인) 전체 조회 (+ adStatus에 따라 필터링 적용)
    public AdsResponse getAllAds(Long advertiserNo, AdStatus adStatus) {
        Advertiser advertiser = advertiserRepository.findByAdvertiserNo(advertiserNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage()));

        List<Advertisement> ads;
        if (adStatus != null) {
            ads = adRepository.findByAdvertiserAndAdStatus(advertiser, adStatus);
        } else {
            ads = adRepository.findByAdvertiser(advertiser);
        }

        if (ads.isEmpty()) {
            throw new RuntimeException(ErrorCode.AD_NOT_MATCHED.getDefaultMessage());
        }

        return AdsResponse.from(ads);
    }

    // 2-4. adStatus별 광고(캠페인) 전체 조회

    // 3-1. 광고(캠페인) 수정 - 광고주
    public AdResponse updateAd(Long adNo, AdRequest request) {
        Advertisement ad = adRepository.findByAdNo(adNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.AD_NOT_FOUND.getDefaultMessage()));

        modify(ad, request);
        Advertisement updatedAd = adRepository.save(ad);

        return AdResponse.from(updatedAd);
    }

    // 3-2. 광고(캠페인) 수정 (AdStatus: APPROVED/REJECTED, (선택:반려 사유 추가)) - 관리자

    // 4. 광고(캠페인) 삭제
    public void deleteAd(Long adNo) {
        Advertisement ad = adRepository.findByAdNo(adNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.AD_NOT_FOUND.getDefaultMessage()));

        adRepository.delete(ad);
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

    private void modify(Advertisement ad, AdRequest request) {
        ad.setTitle(request.getTitle());
        ad.setContent(request.getContent());
        ad.setObjective(request.getObjective());
        ad.setAnnounceStart(request.getAnnounceStart());
        ad.setAnnounceEnd(request.getAnnounceEnd());
        ad.setCampaignSelect(request.getCampaignSelect());
        ad.setCampaignStart(request.getCampaignStart());
        ad.setCampaignEnd(request.getCampaignEnd());
        ad.setMembers(request.getMembers());
        ad.setAdUrl(request.getAdUrl());

        modifyMissions(ad, request.getMission());
        modifyKeywords(ad, request.getKeyword());
        modifyRequirements(ad, request.getRequirement());
    }

    private void modifyMissions(Advertisement ad, List<MissionRequest> newMissions) {
        List<Mission> existingMissions = ad.getMission();
        if (existingMissions == null) {
            existingMissions = new ArrayList<>();
        }
        if (newMissions == null) {
            newMissions = Collections.emptyList();
        }
        int existingSize = existingMissions.size();
        int newSize = newMissions.size();

        int minSize = Math.min(existingSize, newSize);
        for (int i = 0; i < minSize; i++) {
            existingMissions.get(i).setContent(newMissions.get(i).getContent());
        }
        if (existingSize > newSize) {
            existingMissions.subList(newSize, existingSize).clear();
        } else if (existingSize < newSize) {
            for (int i = existingSize; i < newSize; i++) {
                Mission mission = new Mission();
                mission.setContent(newMissions.get(i).getContent());
                mission.setAdvertisement(ad);
                existingMissions.add(mission);
            }
        }
        ad.setMission(existingMissions);
    }

    private void modifyKeywords(Advertisement ad, List<KeywordRequest> newKeywords) {
        List<Keyword> existingKeywords = ad.getKeyword();
        if (existingKeywords == null) {
            existingKeywords = new ArrayList<>();
        }
        if (newKeywords == null) {
            newKeywords = Collections.emptyList();
        }
        int existingSize = existingKeywords.size();
        int newSize = newKeywords.size();

        int minSize = Math.min(existingSize, newSize);
        for (int i = 0; i < minSize; i++) {
            existingKeywords.get(i).setContent(newKeywords.get(i).getContent());
        }
        if (existingSize > newSize) {
            existingKeywords.subList(newSize, existingSize).clear();
        } else if (existingSize < newSize) {
            for (int i = existingSize; i < newSize; i++) {
                Keyword keyword = new Keyword();
                keyword.setContent(newKeywords.get(i).getContent());
                keyword.setAdvertisement(ad);
                existingKeywords.add(keyword);
            }
        }
        ad.setKeyword(existingKeywords);
    }

    private void modifyRequirements(Advertisement ad, List<RequirementRequest> newRequirements) {
        List<Requirement> existingRequirements = ad.getRequirement();
        if (existingRequirements == null) {
            existingRequirements = new ArrayList<>();
        }
        if (newRequirements == null) {
            newRequirements = Collections.emptyList();
        }
        int existingSize = existingRequirements.size();
        int newSize = newRequirements.size();

        int minSize = Math.min(existingSize, newSize);
        for (int i = 0; i < minSize; i++) {
            existingRequirements.get(i).setContent(newRequirements.get(i).getContent());
        }
        if (existingSize > newSize) {
            existingRequirements.subList(newSize, existingSize).clear();
        } else if (existingSize < newSize) {
            for (int i = existingSize; i < newSize; i++) {
                Requirement requirement = new Requirement();
                requirement.setContent(newRequirements.get(i).getContent());
                requirement.setAdvertisement(ad);
                existingRequirements.add(requirement);
            }
        }
        ad.setRequirement(existingRequirements);
    }
}
