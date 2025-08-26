package site.petful.healthservice.medical.medication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.healthservice.medical.medication.entity.ScheduleMedDetail;

@Repository
public interface ScheduleMedicationDetailRepository extends JpaRepository<ScheduleMedDetail, Long> {
    
    /**
     * 엔티티를 저장하고 즉시 DB에 반영
     */
    @Override
    <S extends ScheduleMedDetail> S saveAndFlush(S entity);
}


