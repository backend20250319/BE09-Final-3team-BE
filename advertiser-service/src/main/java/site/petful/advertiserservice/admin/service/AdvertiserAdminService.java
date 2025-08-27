package site.petful.advertiserservice.admin.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.advertiserservice.entity.Advertiser;
import site.petful.advertiserservice.entity.advertisement.AdStatus;
import site.petful.advertiserservice.entity.advertisement.Advertisement;
import site.petful.advertiserservice.repository.AdRepository;
import site.petful.advertiserservice.repository.AdvertiserRepository;
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdvertiserAdminService {
    private final AdvertiserRepository advertiserRepository;
    private final AdRepository adRepository;
    // 광고주 제한
    public void restrictAdvertiser(Long id) {
        Advertiser restrictAdvertiser = advertiserRepository.findById(id)
                .orElseThrow(()->new NotFoundException("해당 광고주를 찾을 수 없습니다."));
        restrictAdvertiser.suspend();
    }
    // 광고주 제한 거절
    public void rejectAdvertiser(Long advertiserId,String reason) {
        Advertiser rejectAdvertiser = advertiserRepository.findById(advertiserId)
                .orElseThrow(()->new NotFoundException("해당 광고주를 찾을 수 없습니다."));
        rejectAdvertiser.setReason(reason);
    }

    public void approveAdvertiser(Long advertiserId) {
        Advertiser approveAdvertiser = advertiserRepository.findById(advertiserId)
                .orElseThrow(()->new NotFoundException("해당 광고주를 찾을 수 없습니다."));
        approveAdvertiser.setIsApproved(true);
    }

    public Page<Advertiser> getAllAdvertiser(Pageable pageable) {
        return advertiserRepository.findByIsApproved(false,pageable);
    }

    public Page<Advertisement> getAllCampaign(Pageable pageable) {
        return adRepository.findByAdStatus(AdStatus.TRIAL, pageable);
    }

    public void deleteCampaign(Long adId) {
        Advertisement ad = adRepository.findById(adId)
                .orElseThrow(()->new IllegalArgumentException("해당 광고를 찾을 수 없습니다."));
        if(ad.getAdStatus() == AdStatus.REJECTED){
            return;
        }
        ad.setAdStatus(AdStatus.REJECTED);
    }

    public Page<Advertisement> getPendingAds(Pageable pageable) {
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
