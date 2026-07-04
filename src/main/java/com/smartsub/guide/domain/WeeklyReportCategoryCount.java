package com.smartsub.guide.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_weekly_report_category_counts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyReportCategoryCount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_report_id", nullable = false)
    private WeeklyReport weeklyReport;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ChatCategory category;

    @Column(name = "count", nullable = false)
    private Long count;

    private WeeklyReportCategoryCount(WeeklyReport weeklyReport, ChatCategory category, Long count) {
        this.weeklyReport = weeklyReport;
        this.category = category;
        this.count = count;
    }

    public static WeeklyReportCategoryCount create(WeeklyReport weeklyReport, ChatCategory category, Long count) {
        return new WeeklyReportCategoryCount(weeklyReport, category, count);
    }
}