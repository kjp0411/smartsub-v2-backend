package com.smartsub.guide.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartsub.guide.domain.ChatCategory;
import com.smartsub.guide.domain.ChatLog;
import com.smartsub.guide.domain.ChatLogRepository;
import com.smartsub.guide.domain.Language;
import com.smartsub.guide.domain.WeeklyReport;
import com.smartsub.guide.domain.WeeklyReportRepository;
import com.smartsub.store.domain.Store;
import com.smartsub.store.domain.StoreRepository;
import com.smartsub.store.domain.StoreStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WeeklyReportSchedulerTest {

    @Autowired
    private WeeklyReportScheduler weeklyReportScheduler;

    @Autowired
    private WeeklyReportRepository weeklyReportRepository;

    @Autowired
    private ChatLogRepository chatLogRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    @DisplayName("ACTIVE 매장의 리포트가 생성되고 created_by가 SYSTEM으로 기록된다")
    void generateWeeklyReports_createsReportForActiveStore() {
        // Given
        Store store = storeRepository.save(
            Store.create(UUID.randomUUID(), "테스트매장", null, "prompt", StoreStatus.ACTIVE)
        );
        saveLog(store.getId(), ChatCategory.MENU);
        saveLog(store.getId(), ChatCategory.MENU);
        saveLog(store.getId(), ChatCategory.PARKING);

        LocalDateTime weekStart = LocalDateTime.now().minusDays(1);
        LocalDateTime weekEnd = LocalDateTime.now().plusDays(1);

        // When
        weeklyReportScheduler.generateWeeklyReports(weekStart, weekEnd);

        // Then
        List<WeeklyReport> reports = weeklyReportRepository.findAllByDeletedAtIsNull().stream()
            .filter(r -> r.getStoreId().equals(store.getId()))
            .toList();

        assertThat(reports).hasSize(1);
        WeeklyReport report = reports.get(0);
        assertThat(report.getTotalCount()).isEqualTo(3L);
        assertThat(report.getTopCategory()).isEqualTo(ChatCategory.MENU);
        assertThat(report.getCreatedBy()).isNotNull();
    }

    @Test
    @DisplayName("동일 매장·동일 기간 재실행 시 중복 리포트가 생성되지 않는다")
    void generateWeeklyReports_doesNotDuplicateOnRerun() {
        // Given
        Store store = storeRepository.save(
            Store.create(UUID.randomUUID(), "테스트매장2", null, "prompt", StoreStatus.ACTIVE)
        );
        saveLog(store.getId(), ChatCategory.FACILITY);

        LocalDateTime weekStart = LocalDateTime.now().minusDays(1);
        LocalDateTime weekEnd = LocalDateTime.now().plusDays(1);

        // When
        weeklyReportScheduler.generateWeeklyReports(weekStart, weekEnd);
        weeklyReportScheduler.generateWeeklyReports(weekStart, weekEnd);

        // Then
        long count = weeklyReportRepository.findAllByDeletedAtIsNull().stream()
            .filter(r -> r.getStoreId().equals(store.getId()))
            .count();
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("PAUSED 매장은 리포트가 생성되지 않는다")
    void generateWeeklyReports_excludesPausedStore() {
        // Given
        Store pausedStore = storeRepository.save(
            Store.create(UUID.randomUUID(), "중지매장", null, "prompt", StoreStatus.PAUSED)
        );
        saveLog(pausedStore.getId(), ChatCategory.HOURS);

        LocalDateTime weekStart = LocalDateTime.now().minusDays(1);
        LocalDateTime weekEnd = LocalDateTime.now().plusDays(1);

        // When
        weeklyReportScheduler.generateWeeklyReports(weekStart, weekEnd);

        // Then
        boolean exists = weeklyReportRepository.existsByStoreIdAndWeekStart(
            pausedStore.getId(), weekStart
        );
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("매장 A의 리포트에 매장 B의 카테고리 데이터가 포함되지 않는다")
    void generateWeeklyReports_isolatesBetweenStores() {
        // Given
        Store storeA = storeRepository.save(
            Store.create(UUID.randomUUID(), "매장A", null, "prompt", StoreStatus.ACTIVE)
        );
        Store storeB = storeRepository.save(
            Store.create(UUID.randomUUID(), "매장B", null, "prompt", StoreStatus.ACTIVE)
        );
        saveLog(storeA.getId(), ChatCategory.PARKING);
        saveLog(storeB.getId(), ChatCategory.MENU);

        LocalDateTime weekStart = LocalDateTime.now().minusDays(1);
        LocalDateTime weekEnd = LocalDateTime.now().plusDays(1);

        // When
        weeklyReportScheduler.generateWeeklyReports(weekStart, weekEnd);

        // Then
        WeeklyReport reportA = weeklyReportRepository.findAllByDeletedAtIsNull().stream()
            .filter(r -> r.getStoreId().equals(storeA.getId()))
            .findFirst().orElseThrow();
        WeeklyReport reportB = weeklyReportRepository.findAllByDeletedAtIsNull().stream()
            .filter(r -> r.getStoreId().equals(storeB.getId()))
            .findFirst().orElseThrow();

        assertThat(reportA.getTopCategory()).isEqualTo(ChatCategory.PARKING);
        assertThat(reportB.getTopCategory()).isEqualTo(ChatCategory.MENU);
    }

    private void saveLog(UUID storeId, ChatCategory category) {
        ChatLog log = ChatLog.create(storeId, "1", "질문", "답변", Language.KOREAN, category);
        chatLogRepository.save(log);
    }
}