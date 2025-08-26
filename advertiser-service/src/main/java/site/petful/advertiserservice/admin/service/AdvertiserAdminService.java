package site.petful.advertiserservice.admin.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.advertiserservice.entity.Advertiser;
import site.petful.advertiserservice.repository.AdvertiserRepository;
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdvertiserAdminService {
    private final AdvertiserRepository advertiserRepository;

    public void restrictAdvertiser(Long id) {
        Advertiser restrictAdvertiser = advertiserRepository.findById(id)
                .orElseThrow(()->new NotFoundException("해당 광고주를 찾을 수 없습니다."));
        restrictAdvertiser.suspend();
    }

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
}
