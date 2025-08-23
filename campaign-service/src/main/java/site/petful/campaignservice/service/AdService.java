package site.petful.campaignservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.campaignservice.client.AdvertiserFeignClient;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.dto.advertisement.AdWithPetNosResponse;
import site.petful.campaignservice.dto.advertisement.AdsGroupedResponse;
import site.petful.campaignservice.dto.advertisement.AppliedAdsResponse;
import site.petful.campaignservice.dto.campaign.PetResponse;
import site.petful.campaignservice.entity.Applicant;
import site.petful.campaignservice.entity.advertisement.Advertisement;
import site.petful.campaignservice.repository.AdRepository;
import site.petful.campaignservice.repository.CampaignRepository;
import site.petful.campaignservice.repository.PetRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdService {

    private final AdRepository adRepository;
    private final PetRepository petRepository;
    private final AdvertiserFeignClient advertiserFeignClient;
    private final CampaignRepository campaignRepository;

    // 1. adStatus별(모집중/종료된) 광고(캠페인) 전체 조회
    public AdsGroupedResponse getAdsByStatusGrouped() {
        ApiResponse<AdsGroupedResponse> response = advertiserFeignClient.getAdsGroupedByAdStatus();
        return response.getData();
    }

    // 2. 신청한 광고(캠페인) 전체 조회
    public AppliedAdsResponse getAppliedAds(Long userNo) {
        List<PetResponse> pets = petRepository.findByUserNo(userNo);

        List<Long> allPetNos = pets.stream()
                .map(PetResponse::getPetNo)
                .collect(Collectors.toList());

        List<Applicant> applicants = campaignRepository.findByPetNoIn(allPetNos);

        Map<Long, List<Long>> adNoToPetNos = applicants.stream()
                .collect(Collectors.groupingBy(
                        applicant -> applicant.getAdvertisement().getAdNo(),
                        Collectors.mapping(Applicant::getPetNo, Collectors.toList())
                ));

        List<Advertisement> ads = applicants.stream()
                .map(Applicant::getAdvertisement)
                .distinct()
                .toList();

        List<AdWithPetNosResponse> adsWithPetNos = ads.stream()
                .map(ad -> {
                    List<Long> petNos = adNoToPetNos.getOrDefault(ad.getAdNo(), Collections.emptyList());
                    return AdWithPetNosResponse.from(ad, petNos);
                })
                .collect(Collectors.toList());

        return AppliedAdsResponse.from(adsWithPetNos);
    }
}
