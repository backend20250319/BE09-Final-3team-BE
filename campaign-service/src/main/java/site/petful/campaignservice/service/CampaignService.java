package site.petful.campaignservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;;
import site.petful.campaignservice.common.ErrorCode;
import site.petful.campaignservice.dto.campaign.CampaignRequest;
import site.petful.campaignservice.dto.campaign.CampaignResponse;
import site.petful.campaignservice.entity.Applicant;
import site.petful.campaignservice.entity.ApplicantStatus;
import site.petful.campaignservice.entity.Pet;
import site.petful.campaignservice.entity.advertisement.Advertisement;
import site.petful.campaignservice.repository.AdRepository;
import site.petful.campaignservice.repository.CampaignRepository;

@Service
@RequiredArgsConstructor
public class CampaignService {

    public final CampaignRepository campaignRepository;
    public final AdRepository adRepository;

    // 1. 체험단 신청
    public CampaignResponse applyAd(Long adNo, Long petNo, CampaignRequest request) {

        Advertisement ad = adRepository.findByAdNo(adNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.AD_NOT_FOUND.getDefaultMessage()));

        Pet dummyPet = new Pet(1L, "황금이", "골든리트리버", 3, 'F', false);

        Applicant applicant = new Applicant();
        applicant.setAdvertisement(ad);
        applicant.setPet(dummyPet);
        applicant.setContent(request.getContent());
        applicant.setStatus(applicant.getStatus() == null ? ApplicantStatus.APPLIED : applicant.getStatus());
        Applicant saved = campaignRepository.save(applicant);

        return CampaignResponse.from(saved);
    }
}
