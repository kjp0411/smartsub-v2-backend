package com.smartsub.guide.domain;

import com.smartsub.global.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;

@Filter(name = "tenantFilter", condition = "store_id = :storeId")
@Getter
@Entity
@Table(
    name = "p_weekly_reports",
    uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "week_start"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "store_id", nullable = false, updatable = false)
    private UUID storeId;

    @Column(name = "week_start", nullable = false, updatable = false)
    private LocalDateTime weekStart;

    @Column(name = "week_end", nullable = false, updatable = false)
    private LocalDateTime weekEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "top_category")
    private ChatCategory topCategory;

    @Column(name = "total_count", nullable = false)
    private Long totalCount;

    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeeklyReportCategoryCount> categoryCounts = new ArrayList<>();

    private WeeklyReport(UUID storeId, LocalDateTime weekStart, LocalDateTime weekEnd) {
        this.storeId = storeId;
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.totalCount = 0L;
    }

    public static WeeklyReport create(UUID storeId, LocalDateTime weekStart, LocalDateTime weekEnd, Map<ChatCategory, Long> categoryCounts) {
        WeeklyReport report = new WeeklyReport(storeId, weekStart, weekEnd);
        report.applyCategoryCounts(categoryCounts);
        return report;
    }

    private void applyCategoryCounts(Map<ChatCategory, Long> counts) {
        long total = 0;
        ChatCategory top = null;
        long maxCount = -1;

        for (Map.Entry<ChatCategory, Long> entry : counts.entrySet()) {
            ChatCategory category = entry.getKey();
            Long count = entry.getValue();

            this.categoryCounts.add(WeeklyReportCategoryCount.create(this, category, count));
            total += count;

            if (count > maxCount) {
                maxCount = count;
                top = category;
            }
        }

        this.totalCount = total;
        this.topCategory = top;
    }
}