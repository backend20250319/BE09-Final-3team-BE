package site.petful.campaignservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.campaignservice.client.AdvertiserFeignClient;
import site.petful.campaignservice.client.PetFeignClient;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.dto.advertisement.*;
import site.petful.campaignservice.dto.pet.PetResponse;
import site.petful.campaignservice.entity.Applicant;
import site.petful.campaignservice.repository.CampaignRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdService {

    private final AdvertiserFeignClient advertiserFeignClient;
    private final PetFeignClient petFeignClient;
    private final CampaignRepository campaignRepository;

    // 1. adStatus별(모집중/종료된) 광고(캠페인) 전체 조회
    public AdsGroupedResponse getAdsByStatusGrouped() {
        ApiResponse<AdsGroupedResponse> response = advertiserFeignClient.getAllAdsByAdStatusGrouped();
        return response.getData();
    }

    // 2. 신청한 광고(캠페인) 전체 조회
    public AppliedAdsResponse getAppliedAds(Long userNo) {
        ApiResponse<List<PetResponse>> pets = petFeignClient.getPetsExternal(userNo);

        List<Long> allPetNos = Optional.ofNullable(pets)
                .map(ApiResponse::getData)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(PetResponse::getPetNo)
                .collect(Collectors.toList());

        List<Applicant> applicants = campaignRepository.findByPetNoIn(allPetNos);

        Map<Long, List<Long>> adNoToPetNos = applicants.stream()
                .collect(Collectors.groupingBy(
                        Applicant::getAdNo,
                        Collectors.mapping(Applicant::getPetNo, Collectors.toList())
                ));

        List<Long> adNos = applicants.stream()
                .map(Applicant::getAdNo)
                .distinct()
                .toList();

        List<AdResponse> ads = advertiserFeignClient.getAdsByAdNos(adNos).getData().getAds();

        List<AdWithPetNosResponse> adsWithPetNos = ads.stream()
                .map(ad -> {
                    List<Long> petNos = adNoToPetNos.getOrDefault(ad.getAdNo(), Collections.emptyList());
                    return AdWithPetNosResponse.from(ad, petNos);
                })
                .collect(Collectors.toList());

        return AppliedAdsResponse.from(adsWithPetNos);
    }

    // 3-1. 광고 이미지 조회
    public ImageUploadResponse getImageByAdNo(Long adNo) {
        return advertiserFeignClient.getImageByAdNo(adNo).getData();
    }

    // 3-2. 광고주 파일 조회
    public List<FileUploadResponse> getAdvertiserFile(Long advertiserNo) {
        return advertiserFeignClient.getFileByAdvertiserNo(advertiserNo).getData();
    }

    // 4. 광고 단일 조회
    public AdResponse getAd(Long adNo) {
        return advertiserFeignClient.getAd(adNo).getData();
    }
}
