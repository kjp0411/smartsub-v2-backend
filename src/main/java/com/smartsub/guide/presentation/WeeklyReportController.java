package com.smartsub.guide.presentation;

import com.smartsub.guide.application.WeeklyReportService;
import com.smartsub.guide.application.dto.WeeklyReportResult;
import com.smartsub.guide.presentation.response.WeeklyReportResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    @GetMapping
    public ResponseEntity<List<WeeklyReportResponse>> getReports() {
        List<WeeklyReportResponse> responses = weeklyReportService.getReports()
            .stream()
            .map(WeeklyReportResponse::from)
            .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<WeeklyReportResponse> getReport(
        @PathVariable UUID reportId
    ) {
        WeeklyReportResult result = weeklyReportService.getReport(reportId);

        return ResponseEntity.ok(WeeklyReportResponse.from(result));
    }
}