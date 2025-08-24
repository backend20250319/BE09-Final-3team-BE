package site.petful.healthservice.medical.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.healthservice.medical.entity.CalendarMedDetail;

@Repository
public interface CalendarMedicationDetailRepository extends JpaRepository<CalendarMedDetail, Long> {
}


