package site.petful.userservice.admin.repository;

import aj.org.objectweb.asm.commons.Remapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.userservice.admin.entity.ActorType;
import site.petful.userservice.admin.entity.ReportLog;
import site.petful.userservice.admin.entity.ReportStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportLogRepository extends JpaRepository<ReportLog,Long>{


    Page<ReportLog> findByTarget_TypeAndReportStatusOrderByCreatedAtDesc(ActorType targetType, ReportStatus status, Pageable pageable);

    Page<ReportLog> findByTarget_TypeOrderByCreatedAtDesc(ActorType targetType, Pageable pageable);

    Page<ReportLog> findByReportStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    Page<ReportLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
