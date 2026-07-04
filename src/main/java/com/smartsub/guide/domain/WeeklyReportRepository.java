package com.smartsub.guide.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface WeeklyReportRepository {

    WeeklyReport save(WeeklyReport weeklyReport);

    boolean existsByStoreIdAndWeekStart(UUID storeId, LocalDateTime weekStart);

    List<WeeklyReport> findAll();
}