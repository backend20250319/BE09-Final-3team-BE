package site.petful.campaignservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;;
import site.petful.campaignservice.client.AdvertiserFeignClient;
import site.petful.campaignservice.common.ErrorCode;
import site.petful.campaignservice.dto.campaign.ApplicantResponse;
import site.petful.campaignservice.dto.campaign.ApplicantsResponse;
import site.petful.campaignservice.dto.campaign.ApplicantRequest;
import site.petful.campaignservice.dto.campaign.PetResponse;
import site.petful.campaignservice.entity.Applicant;
import site.petful.campaignservice.entity.ApplicantStatus;
import site.petful.campaignservice.entity.advertisement.Advertisement;
import site.petful.campaignservice.repository.AdRepository;
import site.petful.campaignservice.repository.CampaignRepository;
import site.petful.campaignservice.repository.PetRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {

    public final CampaignRepository campaignRepository;
    public final AdRepository adRepository;
    private final AdvertiserFeignClient advertiserFeignClient;
    private final PetRepository petRepository;

    // 1. 체험단 신청
    public ApplicantResponse applyCampaign(Long adNo, Long petNo, ApplicantRequest request) {

        Advertisement ad = adRepository.findByAdNo(adNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.AD_NOT_FOUND.getDefaultMessage()));

        Applicant applicant = new Applicant();
        applicant.setAdvertisement(ad);
        applicant.setPetNo(petNo);
        applicant.setContent(request.getContent());
        applicant.setStatus(applicant.getStatus() == null ? ApplicantStatus.APPLIED : applicant.getStatus());
        Applicant saved = campaignRepository.save(applicant);

        PetResponse petResponse = petRepository.findByPetNo(petNo);

        // advertiser의 applicants 1 증가
        advertiserFeignClient.updateAdByCampaign(adNo);

        return ApplicantResponse.from(saved, petResponse);
    }

    // 2. 광고별 체험단 전체 조회 - 광고주
    public ApplicantsResponse getApplicants(Long adNo) {
        Advertisement ad = adRepository.findByAdNo(adNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.AD_NOT_FOUND.getDefaultMessage()));

        List<Applicant> applicants = campaignRepository.findByAdvertisement_AdNo(adNo);

        List<Long> petNos = applicants.stream()
                .map(Applicant::getPetNo)
                .distinct()
                .collect(Collectors.toList());

        List<PetResponse> pets = petRepository.findByPetNos(petNos);
        Map<Long, PetResponse> petMap = pets.stream()
                .collect(Collectors.toMap(PetResponse::getPetNo, pet -> pet));

        List<ApplicantResponse> applicantResponses = applicants.stream()
                .map(applicant -> ApplicantResponse.from(applicant, petMap.get(applicant.getPetNo())))
                .collect(Collectors.toList());

        return ApplicantsResponse.from(ad, applicantResponses);
    }

    // 3-1. 체험단 추가 내용 수정 - 체험단
    public ApplicantResponse updateApplicant(Long applicantNo, ApplicantRequest request) {

        Applicant applicant = campaignRepository.findApplicantByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.APPLICANT_NOT_FOUND.getDefaultMessage()));

        applicant.setContent(request.getContent());
        Applicant saved = campaignRepository.save(applicant);

        PetResponse petResponse = petRepository.findByPetNo(saved.getPetNo());

        return ApplicantResponse.from(saved, petResponse);
    }


    // 3-2. 체험단 applicantStatus 수정 - 광고주
    public ApplicantResponse updateApplicantByAdvertiser(Long applicantNo, ApplicantRequest request) {

        Applicant applicant = campaignRepository.findApplicantByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.APPLICANT_NOT_FOUND.getDefaultMessage()));

        applicant.setStatus(request.getStatus());
        Applicant saved = campaignRepository.save(applicant);

        PetResponse petResponse = petRepository.findByPetNo(saved.getPetNo());

        return ApplicantResponse.from(saved, petResponse);
    }

    // 4. 체험단 신청 취소(삭제)
    public void deleteApplicant(Long applicantNo) {

        Applicant applicant = campaignRepository.findApplicantByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.APPLICANT_NOT_FOUND.getDefaultMessage()));

        campaignRepository.delete(applicant);
    }
}
