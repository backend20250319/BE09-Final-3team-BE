package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.advertiserservice.client.CampaignFeignClient;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.connectNotice.dto.EventMessage;
import site.petful.advertiserservice.dto.campaign.ApplicantRequest;
import site.petful.advertiserservice.dto.campaign.ApplicantsResponse;
import site.petful.advertiserservice.entity.ApplicantStatus;
import site.petful.advertiserservice.entity.advertisement.AdStatus;
import site.petful.advertiserservice.entity.advertisement.Advertisement;
import site.petful.advertiserservice.repository.AdRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatusSchedulerService {

    private final AdRepository adRepository;
    private final CampaignFeignClient campaignFeignClient;
    private final RabbitTemplate rabbitTemplate;

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
            publishCampaignSelectedEvent(ad);
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



    private void publishCampaignSelectedEvent(Advertisement ad) {
        try {
            // 지원자 리스트 조회
            ApiResponse<ApplicantsResponse> response = campaignFeignClient.getApplicants(ad.getAdNo());
            ApplicantsResponse applicants = response.getData();
            
            if (applicants.getApplicants().isEmpty()) {
                log.info("지원자가 없습니다. adNo={}", ad.getAdNo());
                return;
            }
            
    
            List<ApplicantsResponse.ApplicantDetail> selectedApplicants = applicants.getApplicants().stream()
                    .filter(applicant -> applicant.getStatus() == ApplicantStatus.SELECTED)
                    .toList();
            
            if (selectedApplicants.isEmpty()) {
                log.info("선정된 체험단이 없습니다. adNo={}", ad.getAdNo());
                return;
            }

            for (ApplicantsResponse.ApplicantDetail applicant : selectedApplicants) {
                EventMessage event = new EventMessage();
                event.setEventId(UUID.randomUUID().toString());
                event.setType("campaign.applicant.selected");
                event.setOccurredAt(Instant.now());
                event.setSchemaVersion(1);
                
                event.setActor(new EventMessage.Actor(ad.getAdNo(), ad.getTitle()));

                List<EventMessage.Target> targets = new ArrayList<>();
                targets.add(new EventMessage.Target(
                        applicant.getPet().getUserNo(),  // 사용자 ID
                        ad.getAdNo(),                    // 광고 ID
                        "CAMPAIGN"                       // 리소스 타입
                ));
                event.setTarget(targets);

                event.setAttributes(java.util.Map.of(
                        "adTitle", ad.getTitle(),
                        "petName", applicant.getPet().getName(),
                        "applicantNo", applicant.getApplicantNo(),
                        "campaignStart", ad.getCampaignStart().toString(),
                        "campaignEnd", ad.getCampaignEnd().toString()
                ));
                
                // RabbitMQ로 메시지 발송
                rabbitTemplate.convertAndSend("notification.exchange", "campaign.selected", event);
                
                log.info("체험단 선정 알림 발송 완료: adNo={}, applicantNo={}, userId={}", 
                        ad.getAdNo(), applicant.getApplicantNo(), applicant.getPet().getUserNo());
            }
            
        } catch (Exception e) {
            log.error("체험단 선정 이벤트 발행 실패: adNo={}, error={}", 
                    ad.getAdNo(), e.getMessage(), e);
        }
    }
}