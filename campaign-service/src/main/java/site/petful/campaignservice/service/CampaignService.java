package site.petful.campaignservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.campaignservice.client.AdvertiserFeignClient;
import site.petful.campaignservice.client.PetFeignClient;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.common.ErrorCode;
import site.petful.campaignservice.dto.advertisement.AdResponse;
import site.petful.campaignservice.dto.campaign.ApplicantResponse;
import site.petful.campaignservice.dto.campaign.ApplicantRequest;
import site.petful.campaignservice.dto.PetResponse;
import site.petful.campaignservice.dto.campaign.ApplicantsResponse;
import site.petful.campaignservice.entity.Applicant;
import site.petful.campaignservice.entity.ApplicantStatus;
import site.petful.campaignservice.repository.CampaignRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CampaignService {

    public final CampaignRepository campaignRepository;
    private final AdvertiserFeignClient advertiserFeignClient;
    private final PetFeignClient petFeignClient;

    // 1. 체험단 신청
    public ApplicantResponse applyCampaign(Long adNo, Long petNo, ApplicantRequest request) {

        ApiResponse<AdResponse> adResponse = advertiserFeignClient.getAd(adNo);
        if (adResponse == null || adResponse.getData() == null) {
            throw new RuntimeException(ErrorCode.AD_NOT_FOUND.getDefaultMessage());
        }

        ApiResponse<PetResponse> petResponse = petFeignClient.getPet(petNo);
        PetResponse pet = petResponse.getData();

        Applicant applicant = new Applicant();
        applicant.setAdNo(adNo);
        applicant.setPetNo(petNo);
        applicant.setContent(request.getContent());
        applicant.setStatus(applicant.getStatus() == null ? ApplicantStatus.APPLIED : applicant.getStatus());
        Applicant saved = campaignRepository.save(applicant);

        // advertiser의 applicants 1 증가
        advertiserFeignClient.updateAdByCampaign(adNo, 1);

        return ApplicantResponse.from(saved, pet);
    }

    // 2. 광고별 체험단 전체 조회 - 광고주
    @Transactional(readOnly = true)
    public ApplicantsResponse getApplicants(Long adNo) {
        ApiResponse<AdResponse> adResponse = advertiserFeignClient.getAd(adNo);
        if (adResponse == null || adResponse.getData() == null) {
            throw new RuntimeException(ErrorCode.AD_NOT_FOUND.getDefaultMessage());
        }

        List<Applicant> applicants = campaignRepository.findByAdNo(adNo);

        List<Long> petNos = applicants.stream()
                .map(Applicant::getPetNo)
                .distinct()
                .toList();

        List<PetResponse> pets = petFeignClient.getPetsByPetNos(petNos).getData();

        Map<Long, PetResponse> petMap = pets.stream()
                .collect(Collectors.toMap(PetResponse::getPetNo, pet -> pet));

        List<ApplicantResponse> applicantResponses = applicants.stream()
                .map(applicant -> ApplicantResponse.from(applicant, petMap.get(applicant.getPetNo())))
                .collect(Collectors.toList());

        return ApplicantsResponse.from(applicantResponses);
    }

    // 3-1. 체험단 추가 내용 수정 - 체험단
    public ApplicantResponse updateApplicant(Long applicantNo, ApplicantRequest request) {

        Applicant applicant = campaignRepository.findApplicantByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.APPLICANT_NOT_FOUND.getDefaultMessage()));

        applicant.setContent(request.getContent());
        Applicant saved = campaignRepository.save(applicant);

        ApiResponse<PetResponse> petResponse = petFeignClient.getPet(saved.getPetNo());
        PetResponse pet = petResponse.getData();

        return ApplicantResponse.from(saved, pet);
    }


    // 3-2. 체험단 applicantStatus 수정 - 광고주
    public ApplicantResponse updateApplicantByAdvertiser(Long applicantNo, ApplicantStatus status) {

        Applicant applicant = campaignRepository.findApplicantByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.APPLICANT_NOT_FOUND.getDefaultMessage()));

        applicant.setStatus(status);
        Applicant saved = campaignRepository.save(applicant);

        ApiResponse<PetResponse> petResponse = petFeignClient.getPet(saved.getPetNo());
        PetResponse pet = petResponse.getData();

        return ApplicantResponse.from(saved, pet);
    }

    // 4. 체험단 신청 취소(삭제)
    public void deleteApplicant(Long applicantNo) {

        Applicant applicant = campaignRepository.findApplicantByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.APPLICANT_NOT_FOUND.getDefaultMessage()));

        campaignRepository.delete(applicant);

        // advertiser의 applicants 1 감소
        advertiserFeignClient.updateAdByCampaign(applicant.getAdNo(), -1);
    }
}
