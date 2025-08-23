package site.petful.campaignservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.campaignservice.entity.Applicant;

import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Applicant, Long> {
    Optional<Applicant> findApplicantByApplicantNo(Long applicantNo);
}
