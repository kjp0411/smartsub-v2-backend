package com.smartsub.guide.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartsub.guide.domain.ChatCategory;
import com.smartsub.guide.domain.ChatLog;
import com.smartsub.guide.domain.ChatLogRepository;
import com.smartsub.guide.domain.Language;
import com.smartsub.store.domain.Store;
import com.smartsub.store.domain.StoreRepository;
import com.smartsub.store.domain.StoreStatus;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InsightAggregationServiceTest {

    @Autowired
    private InsightAggregationService insightAggregationService;

    @Autowired
    private ChatLogRepository chatLogRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    @DisplayName("기간 범위 밖의 로그는 집계에서 제외된다")
    void aggregate_excludesLogsOutsidePeriod() {
        // Given
        Store store = storeRepository.save(
            Store.create(UUID.randomUUID(), "테스트매장", null, "prompt", StoreStatus.ACTIVE)
        );
        saveLog(store.getId(), ChatCategory.MENU); // 지금 시각으로 저장됨

        LocalDateTime farFutureStart = LocalDateTime.now().plusDays(10);
        LocalDateTime farFutureEnd = LocalDateTime.now().plusDays(20);

        // When
        Map<UUID, Map<ChatCategory, Long>> result =
            insightAggregationService.aggregateWeeklyCategoryCounts(farFutureStart, farFutureEnd);

        // Then
        assertThat(result).doesNotContainKey(store.getId());
    }

    @Test
    @DisplayName("기간 범위 안의 로그는 집계에 포함된다")
    void aggregate_includesLogsInsidePeriod() {
        // Given
        Store store = storeRepository.save(
            Store.create(UUID.randomUUID(), "테스트매장", null, "prompt", StoreStatus.ACTIVE)
        );
        saveLog(store.getId(), ChatCategory.MENU);

        LocalDateTime weekStart = LocalDateTime.now().minusDays(1);
        LocalDateTime weekEnd = LocalDateTime.now().plusDays(1);

        // When
        Map<UUID, Map<ChatCategory, Long>> result =
            insightAggregationService.aggregateWeeklyCategoryCounts(weekStart, weekEnd);

        // Then
        assertThat(result.get(store.getId()).get(ChatCategory.MENU)).isEqualTo(1L);
    }

    @Test
    @DisplayName("PAUSED, TERMINATED 매장은 집계 대상에서 제외된다")
    void aggregate_excludesInactiveStores() {
        // Given
        Store activeStore = storeRepository.save(
            Store.create(UUID.randomUUID(), "활성매장", null, "prompt", StoreStatus.ACTIVE)
        );
        Store pausedStore = storeRepository.save(
            Store.create(UUID.randomUUID(), "중지매장", null, "prompt", StoreStatus.PAUSED)
        );
        LocalDateTime weekStart = LocalDateTime.now().minusDays(1);
        LocalDateTime weekEnd = LocalDateTime.now().plusDays(1);

        saveLog(activeStore.getId(), ChatCategory.MENU);
        saveLog(pausedStore.getId(), ChatCategory.MENU);

        // When
        Map<UUID, Map<ChatCategory, Long>> result =
            insightAggregationService.aggregateWeeklyCategoryCounts(weekStart, weekEnd);

        // Then
        assertThat(result).containsKey(activeStore.getId());
        assertThat(result).doesNotContainKey(pausedStore.getId());
    }

    @Test
    @DisplayName("category가 null인 로그는 집계에서 제외된다")
    void aggregate_excludesNullCategory() {
        // Given
        Store store = storeRepository.save(
            Store.create(UUID.randomUUID(), "테스트매장", null, "prompt", StoreStatus.ACTIVE)
        );
        LocalDateTime weekStart = LocalDateTime.now().minusDays(1);
        LocalDateTime weekEnd = LocalDateTime.now().plusDays(1);

        saveLog(store.getId(), ChatCategory.MENU);
        saveLog(store.getId(), null);

        // When
        Map<UUID, Map<ChatCategory, Long>> result =
            insightAggregationService.aggregateWeeklyCategoryCounts(weekStart, weekEnd);

        // Then
        long totalCount = result.get(store.getId()).values().stream()
            .mapToLong(Long::longValue).sum();
        assertThat(totalCount).isEqualTo(1L); // null 제외하고 MENU 1건만
    }

    @Test
    @DisplayName("매장 A의 집계에 매장 B의 로그가 포함되지 않는다")
    void aggregate_isolatesBetweenStores() {
        // Given
        Store storeA = storeRepository.save(
            Store.create(UUID.randomUUID(), "매장A", null, "prompt", StoreStatus.ACTIVE)
        );
        Store storeB = storeRepository.save(
            Store.create(UUID.randomUUID(), "매장B", null, "prompt", StoreStatus.ACTIVE)
        );
        LocalDateTime weekStart = LocalDateTime.now().minusDays(1);
        LocalDateTime weekEnd = LocalDateTime.now().plusDays(1);

        saveLog(storeA.getId(), ChatCategory.PARKING);
        saveLog(storeB.getId(), ChatCategory.MENU);

        // When
        Map<UUID, Map<ChatCategory, Long>> result =
            insightAggregationService.aggregateWeeklyCategoryCounts(weekStart, weekEnd);

        // Then
        assertThat(result.get(storeA.getId())).doesNotContainKey(ChatCategory.MENU);
        assertThat(result.get(storeB.getId())).doesNotContainKey(ChatCategory.PARKING);
    }

    private void saveLog(UUID storeId, ChatCategory category) {
        ChatLog log = ChatLog.create(storeId, "1", "질문", "답변", Language.KOREAN, category);
        chatLogRepository.save(log);
    }
}