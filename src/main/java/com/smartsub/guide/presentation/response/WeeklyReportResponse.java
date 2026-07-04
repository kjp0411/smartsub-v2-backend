package com.smartsub.guide.presentation.response;

import com.smartsub.guide.application.dto.WeeklyReportResult;
import com.smartsub.guide.domain.ChatCategory;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record WeeklyReportResponse(
    UUID reportId,
    LocalDateTime weekStart,
    LocalDateTime weekEnd,
    Long totalCount,
    ChatCategory topCategory,
    Map<ChatCategory, Long> categoryDistribution
) {
    public static WeeklyReportResponse from(WeeklyReportResult result) {
        return new WeeklyReportResponse(
            result.reportId(),
            result.weekStart(),
            result.weekEnd(),
            result.totalCount(),
            result.topCategory(),
            result.categoryDistribution()
        );
    }
}