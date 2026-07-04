package com.smartsub.guide.infrastructure;

import com.smartsub.global.config.SystemAuthRunner;
import com.smartsub.guide.application.InsightAggregationService;
import com.smartsub.guide.domain.ChatCategory;
import com.smartsub.guide.domain.WeeklyReport;
import com.smartsub.guide.domain.WeeklyReportRepository;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyReportScheduler {

    private final InsightAggregationService insightAggregationService;
    private final WeeklyReportRepository weeklyReportRepository;
    private final SystemAuthRunner systemAuthRunner;

    // 매주 월요일 새벽 3시 실행 — 지난 한 주(월요일 자정 ~ 이번 월요일 자정)를 집계
    @Scheduled(cron = "0 0 3 * * MON")
    public void generateWeeklyReports() {
        LocalDateTime weekEnd = LocalDateTime.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .with(LocalTime.MIDNIGHT);
        LocalDateTime weekStart = weekEnd.minusWeeks(1);

        generateWeeklyReports(weekStart, weekEnd);
    }

    // 임의의 기간에 대해 리포트를 생성 — 테스트 및 수동 재실행(장애 복구 등)에 사용
    public void generateWeeklyReports(LocalDateTime weekStart, LocalDateTime weekEnd) {
        long start = System.currentTimeMillis();

        systemAuthRunner.runAsSystem(() -> createReportsForPeriod(weekStart, weekEnd));

        long elapsed = System.currentTimeMillis() - start;
        log.info("주간 리포트 생성 완료: 기간={} ~ {}, 소요시간={}ms", weekStart, weekEnd, elapsed);
    }

    @Transactional
    protected void createReportsForPeriod(LocalDateTime weekStart, LocalDateTime weekEnd) {
        Map<UUID, Map<ChatCategory, Long>> aggregated =
            insightAggregationService.aggregateWeeklyCategoryCounts(weekStart, weekEnd);

        int created = 0;
        int skipped = 0;

        for (Map.Entry<UUID, Map<ChatCategory, Long>> entry : aggregated.entrySet()) {
            UUID storeId = entry.getKey();
            Map<ChatCategory, Long> categoryCounts = entry.getValue();

            if (weeklyReportRepository.existsByStoreIdAndWeekStart(storeId, weekStart)) {
                skipped++;
                continue;
            }

            WeeklyReport report = WeeklyReport.create(storeId, weekStart, weekEnd, categoryCounts);
            weeklyReportRepository.save(report);
            created++;
        }

        log.info("리포트 생성 결과: 생성={}건, 중복스킵={}건, 대상매장={}건", created, skipped, aggregated.size());
    }
}