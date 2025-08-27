package site.petful.campaignservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.campaignservice.entity.Applicant;
import site.petful.campaignservice.entity.advertisement.AdStatus;
import site.petful.campaignservice.entity.advertisement.Advertisement;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Applicant, Long> {
    Optional<Applicant> findApplicantByApplicantNo(Long applicantNo);

    List<Applicant> findByPetNoIn(List<Long> petNos);

    List<Applicant> findByAdvertisement_AdNo(Long adNo);
}
