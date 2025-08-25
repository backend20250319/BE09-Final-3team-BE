package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.dto.advertiser.AdvertiserRequest;
import site.petful.advertiserservice.dto.advertiser.AdvertiserResponse;
import site.petful.advertiserservice.signup.entity.AdvertiserSignup;
import site.petful.advertiserservice.signup.repository.AdvertiserSignupRepository;

@Service
@RequiredArgsConstructor
public class AdvertiserService {

    private final AdvertiserSignupRepository advertiserSignupRepository;

    // 1. 광고주 프로필 정보 조회
    public AdvertiserResponse getAdvertiser(Long advertiserNo) {

        AdvertiserSignup advertiser = advertiserSignupRepository.findByAdvertiserNo(advertiserNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage()));

        return AdvertiserResponse.from(advertiser);
    }

    // 2. 광고주 프로필 정보 수정
    @Transactional
    public AdvertiserResponse updateAdvertiser(Long advertiserNo, AdvertiserRequest updateRequest, MultipartFile imageFile) {

        AdvertiserSignup advertiser = advertiserSignupRepository.findByAdvertiserNo(advertiserNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage()));

        if(updateRequest.getName() != null) advertiser.setName(updateRequest.getName());
        if(updateRequest.getPhone() != null) advertiser.setPhone(updateRequest.getPhone());
        // website는 AdvertiserSignup에 없으므로 제외
        if(updateRequest.getEmail() != null) advertiser.setUserId(updateRequest.getEmail()); // email을 userId로 설정
        if(updateRequest.getDescription() != null) advertiser.setDescription(updateRequest.getDescription());

        AdvertiserSignup saved = advertiserSignupRepository.save(advertiser);

        return AdvertiserResponse.from(saved);
    }
}