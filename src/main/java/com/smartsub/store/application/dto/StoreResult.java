package com.smartsub.store.application.dto;

import com.smartsub.store.domain.Store;
import com.smartsub.store.domain.StoreStatus;
import java.util.UUID;

public record StoreResult(
    UUID storeId,
    UUID userId,
    String name,
    String commonInfo,
    String promptTemplate,
    StoreStatus status
) {
    public static StoreResult from(Store store) {
        return new StoreResult(
            store.getId(),
            store.getUserId(),
            store.getName(),
            store.getCommonInfo(),
            store.getPromptTemplate(),
            store.getStatus()
        );
    }
}