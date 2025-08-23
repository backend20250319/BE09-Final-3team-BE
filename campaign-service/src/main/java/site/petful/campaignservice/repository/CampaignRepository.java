package site.petful.campaignservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.campaignservice.entity.Applicant;

public interface CampaignRepository extends JpaRepository<Applicant, Long> {
}
