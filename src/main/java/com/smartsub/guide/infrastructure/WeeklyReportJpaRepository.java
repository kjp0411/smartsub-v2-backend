package com.smartsub.guide.infrastructure;

import com.smartsub.guide.domain.WeeklyReport;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyReportJpaRepository extends JpaRepository<WeeklyReport, UUID> {

    boolean existsByStoreIdAndWeekStart(UUID storeId, LocalDateTime weekStart);

    List<WeeklyReport> findAllByDeletedAtIsNull();

    Optional<WeeklyReport> findByIdAndDeletedAtIsNull(UUID reportId);
}