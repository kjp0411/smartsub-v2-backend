package com.smartsub.guide.infrastructure;

import com.smartsub.guide.domain.WeeklyReport;
import com.smartsub.guide.domain.WeeklyReportRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WeeklyReportRepositoryImpl implements WeeklyReportRepository {

    private final WeeklyReportJpaRepository weeklyReportJpaRepository;

    @Override
    public WeeklyReport save(WeeklyReport weeklyReport) {
        return weeklyReportJpaRepository.save(weeklyReport);
    }

    @Override
    public boolean existsByStoreIdAndWeekStart(UUID storeId, LocalDateTime weekStart) {
        return weeklyReportJpaRepository.existsByStoreIdAndWeekStart(storeId, weekStart);
    }

    @Override
    public List<WeeklyReport> findAll() {
        return weeklyReportJpaRepository.findAll();
    }
}