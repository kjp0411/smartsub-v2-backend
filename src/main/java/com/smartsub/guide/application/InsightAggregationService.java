package com.smartsub.guide.application;

import com.smartsub.guide.domain.ChatCategory;
import com.smartsub.guide.domain.ChatCategoryCountProjection;
import com.smartsub.guide.domain.ChatLogRepository;
import com.smartsub.store.domain.Store;
import com.smartsub.store.domain.StoreRepository;
import com.smartsub.store.domain.StoreStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InsightAggregationService {

    private final ChatLogRepository chatLogRepository;
    private final StoreRepository storeRepository;

    public Map<UUID, Map<ChatCategory, Long>> aggregateWeeklyCategoryCounts(
        LocalDateTime weekStart, LocalDateTime weekEnd
    ) {
        Set<UUID> activeStoreIds = storeRepository
            .findAllByStatusAndDeletedAtIsNull(StoreStatus.ACTIVE)
            .stream()
            .map(Store::getId)
            .collect(Collectors.toSet());

        List<ChatCategoryCountProjection> rawCounts =
            chatLogRepository.countByStoreAndCategory(weekStart, weekEnd);

        return rawCounts.stream()
            .filter(row -> activeStoreIds.contains(row.getStoreId()))
            .collect(Collectors.groupingBy(
                ChatCategoryCountProjection::getStoreId,
                Collectors.toMap(
                    ChatCategoryCountProjection::getCategory,
                    ChatCategoryCountProjection::getCount
                )
            ));
    }
}
