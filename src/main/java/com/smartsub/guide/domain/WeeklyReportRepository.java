package com.smartsub.guide.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WeeklyReportRepository {

    WeeklyReport save(WeeklyReport weeklyReport);

    boolean existsByStoreIdAndWeekStart(UUID storeId, LocalDateTime weekStart);

    List<WeeklyReport> findAllByDeletedAtIsNull();

    Optional<WeeklyReport> findByIdAndDeletedAtIsNull(UUID reportId);
}