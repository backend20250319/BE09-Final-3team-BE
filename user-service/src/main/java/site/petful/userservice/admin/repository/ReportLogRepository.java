package site.petful.userservice.admin.repository;

import aj.org.objectweb.asm.commons.Remapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.userservice.admin.entity.ReportLog;
import site.petful.userservice.admin.entity.ReportStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportLogRepository extends JpaRepository<ReportLog,Long>{

    Optional<ReportLog> findTopByReporterNoAndTargetNoAndReportStatusOrderByCreatedAtDesc(Long reporterNo, Long targetNo, ReportStatus reportStatus);

    Page<ReportLog> findByStatus(ReportStatus reportStatus, Pageable pageable);

    Optional<ReportLog> findByReporterNoAndTargetNoAndReportStatus(Long reporterId, Long targetId, ReportStatus reportStatus);
}
