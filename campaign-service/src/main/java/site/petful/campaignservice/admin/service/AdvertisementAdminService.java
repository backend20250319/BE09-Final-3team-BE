package site.petful.campaignservice.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import site.petful.campaignservice.dto.advertisement.AdResponse;
import site.petful.campaignservice.dto.advertisement.AdsResponse;
import site.petful.campaignservice.entity.advertisement.AdStatus;
import site.petful.campaignservice.entity.advertisement.Advertisement;
import site.petful.campaignservice.repository.AdRepository;

@Service
@RequiredArgsConstructor
public class AdvertisementAdminService {
    private final AdRepository adRepository;

    public Page<AdsResponse> getAllCampaign(Pageable pageable) {
        return adRepository.findByAdStatus(AdStatus.TRIAL,pageable);
    }

    public void deleteCampaign(Long adId) {
        Advertisement ad = adRepository.findById(adId)
                .orElseThrow(()->new IllegalArgumentException("해당 광고를 찾을 수 없습니다."));
        if(ad.getAdStatus() == AdStatus.REJECTED){
            return;
        }
        ad.setAdStatus(AdStatus.REJECTED);
    }

    public Page<AdsResponse> getPendingAds(Pageable pageable) {
        return adRepository.findByAdStatus(AdStatus.PENDING,pageable);
    }

    public void approve(Long adId) {
        Advertisement ad = adRepository.findByAdNo(adId)
                .orElseThrow(()->new IllegalArgumentException("해당 광고를 찾을 수 없습니다"));
        ad.setAdStatus(AdStatus.APPROVED);
    }

    public void reject(Long adId, String reason) {
        Advertisement ad = adRepository.findByAdNo(adId)
                .orElseThrow(()->new IllegalArgumentException("해당 광고를 찾을 수 없습니다"));
        ad.setAdStatus(AdStatus.REJECTED);
        ad.setReason(reason);
    }
}
