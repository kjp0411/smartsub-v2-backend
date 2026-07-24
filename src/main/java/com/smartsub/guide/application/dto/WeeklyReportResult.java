package com.smartsub.guide.application.dto;

import com.smartsub.guide.domain.ChatCategory;
import com.smartsub.guide.domain.WeeklyReport;
import com.smartsub.guide.domain.WeeklyReportCategoryCount;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public record WeeklyReportResult(
    UUID reportId,
    LocalDateTime weekStart,
    LocalDateTime weekEnd,
    Long totalCount,
    ChatCategory topCategory,
    Map<ChatCategory, Long> categoryDistribution
) {
    public static WeeklyReportResult from(WeeklyReport report) {
        Map<ChatCategory, Long> distribution = report.getCategoryCounts().stream()
            .collect(Collectors.toMap(
                WeeklyReportCategoryCount::getCategory,
                WeeklyReportCategoryCount::getCount
            ));

        return new WeeklyReportResult(
            report.getId(),
            report.getWeekStart(),
            report.getWeekEnd(),
            report.getTotalCount(),
            report.getTopCategory(),
            distribution
        );
    }
}