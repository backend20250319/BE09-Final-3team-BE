package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.advertiserservice.client.CampaignFeignClient;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.dto.campaign.ApplicantRequest;
import site.petful.advertiserservice.dto.campaign.ApplicantsResponse;
import site.petful.advertiserservice.entity.ApplicantStatus;
import site.petful.advertiserservice.entity.advertisement.AdStatus;
import site.petful.advertiserservice.entity.advertisement.Advertisement;
import site.petful.advertiserservice.repository.AdRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatusSchedulerService {

    private final AdRepository adRepository;
    private final CampaignFeignClient campaignFeignClient;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")  // 매일 0시 실행
    public void updateAdStatusByAnnounceEnd() {
        LocalDate targetDate = LocalDate.now().minusDays(1);

        // announceEnd가 어제이고, 상태가 APPROVED인 광고 리스트 조회
        List<Advertisement> targetAds = adRepository.findByAnnounceEndAndAdStatus(targetDate, AdStatus.APPROVED);

        if (targetAds.isEmpty()) {
            return;  // 처리할 광고가 없으면 종료
        }

        // applicant의 status를 PENDING로 변경
        for (Advertisement ad : targetAds) {
            // 지원자 리스트 가져오기
            ApiResponse<ApplicantsResponse> response = campaignFeignClient.getApplicants(ad.getAdNo());
            ApplicantsResponse applicants = response.getData();

            for (ApplicantsResponse.ApplicantDetail applicant : applicants.getApplicants()) {
                ApplicantRequest req = new ApplicantRequest();
                req.setIsSaved(applicant.getIsSaved());
                req.setStatus(ApplicantStatus.PENDING);

                campaignFeignClient.updateApplicant(applicant.getApplicantNo(), req);
            }
        }

        for (Advertisement ad : targetAds) {
            ad.setAdStatus(AdStatus.CLOSED);  // 상태 변경
        }
        adRepository.saveAll(targetAds);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")  // 매일 0시 실행
    public void updateAdStatusByCampaignSelect() {
        LocalDate today = LocalDate.now();

        // campaignSelect가 오늘이고, 상태가 CLOSED인 광고 리스트 조회
        List<Advertisement> targetAds = adRepository.findByCampaignSelectAndAdStatus(today, AdStatus.CLOSED);

        // applicant의 isSaved를 true로 변경
        for (Advertisement ad : targetAds) {
            // 지원자 리스트 가져오기
            ApiResponse<ApplicantsResponse> response = campaignFeignClient.getApplicants(ad.getAdNo());
            ApplicantsResponse applicants = response.getData();

            for (ApplicantsResponse.ApplicantDetail applicant : applicants.getApplicants()) {
                // isSaved가 false인 지원자만 처리
                if (!Boolean.TRUE.equals(applicant.getIsSaved())) {
                    ApplicantRequest req = new ApplicantRequest();
                    req.setIsSaved(true);
                    req.setStatus(applicant.getStatus()); // 기존 상태 유지

                    campaignFeignClient.updateApplicant(applicant.getApplicantNo(), req);
                    if(applicant.getStatus().equals(ApplicantStatus.SELECTED)) {
                        campaignFeignClient.createReview(applicant.getApplicantNo());
                    }
                }
            }
        }

        adRepository.saveAll(targetAds);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")  // 매일 0시 실행
    public void updateAdStatusByCampaignStart() {
        LocalDate today = LocalDate.now();

        // campaignStart가 오늘이고, 상태가 CLOSED인 광고 리스트 조회
        List<Advertisement> targetAds = adRepository.findByCampaignStartAndAdStatus(today, AdStatus.CLOSED);

        if (targetAds.isEmpty()) {
            return;  // 처리할 광고가 없으면 종료
        }

        for (Advertisement ad : targetAds) {
            ad.setAdStatus(AdStatus.TRIAL);  // 상태 변경
        }
        adRepository.saveAll(targetAds);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")  // 매일 0시 실행
    public void updateAdStatusByCampaignEnd() {
        LocalDate targetDate = LocalDate.now().minusDays(1);

        // campaignEnd가 어제이고, 상태가 TRIAL인 광고 리스트 조회
        List<Advertisement> targetAds = adRepository.findByCampaignEndAndAdStatus(targetDate, AdStatus.TRIAL);

        if (targetAds.isEmpty()) {
            return;  // 처리할 광고가 없으면 종료
        }

        for (Advertisement ad : targetAds) {
            ad.setAdStatus(AdStatus.ENDED);  // 상태 변경
        }
        adRepository.saveAll(targetAds);
    }
}
