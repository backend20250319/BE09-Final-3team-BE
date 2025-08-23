package site.petful.campaignservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.campaignservice.entity.advertisement.Advertisement;

public interface CampaignRepository extends JpaRepository<Advertisement, Long> {
}
