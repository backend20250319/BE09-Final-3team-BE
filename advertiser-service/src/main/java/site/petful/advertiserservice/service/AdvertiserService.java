package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.dto.advertiser.AdvertiserRequest;
import site.petful.advertiserservice.dto.advertiser.AdvertiserResponse;
import site.petful.advertiserservice.entity.Advertiser;
import site.petful.advertiserservice.repository.AdvertiserRepository;

@Service
@RequiredArgsConstructor
public class AdvertiserService {

    private final AdvertiserRepository advertiserRepository;

    // 1. 광고주 프로필 정보 조회
    public AdvertiserResponse getAdvertiser(Long advertiserNo) {

        Advertiser advertiser = advertiserRepository.findByAdvertiserNo(advertiserNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage()));

        return AdvertiserResponse.from(advertiser);
    }

    // 2. 광고주 프로필 정보 수정
    public AdvertiserResponse updateAdvertiser(Long advertiserNo, AdvertiserRequest updateRequest, MultipartFile imageFile) {

        Advertiser advertiser = advertiserRepository.findByAdvertiserNo(advertiserNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage()));

        if(updateRequest.getName() != null) advertiser.setName(updateRequest.getName());
        if(updateRequest.getPhone() != null) advertiser.setPhone(updateRequest.getPhone());
        if(updateRequest.getWebsite() != null) advertiser.setWebsite(updateRequest.getWebsite());
        if(updateRequest.getEmail() != null) advertiser.setEmail(updateRequest.getEmail());
        if(updateRequest.getDescription() != null) advertiser.setDescription(updateRequest.getDescription());

        Advertiser saved = advertiserRepository.save(advertiser);

        return AdvertiserResponse.from(saved);
    }
}
