package com.smartsub.guide.application;

import com.smartsub.global.exception.BusinessException;
import com.smartsub.global.exception.ErrorCode;
import com.smartsub.guide.application.dto.WeeklyReportResult;
import com.smartsub.guide.domain.WeeklyReport;
import com.smartsub.guide.domain.WeeklyReportRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyReportService {

    private final WeeklyReportRepository weeklyReportRepository;

    public List<WeeklyReportResult> getReports() {
        return weeklyReportRepository.findAllByDeletedAtIsNull()
            .stream()
            .map(WeeklyReportResult::from)
            .toList();
    }

    public WeeklyReportResult getReport(UUID reportId) {
        WeeklyReport report = weeklyReportRepository.findByIdAndDeletedAtIsNull(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));

        return WeeklyReportResult.from(report);
    }
}