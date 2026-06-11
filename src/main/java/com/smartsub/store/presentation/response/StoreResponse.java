package com.smartsub.store.presentation.response;

import com.smartsub.store.application.dto.StoreResult;
import com.smartsub.store.domain.StoreStatus;
import java.util.UUID;

public record StoreResponse(
    UUID storeId,
    UUID userId,
    String name,
    String commonInfo,
    String promptTemplate,
    StoreStatus status
) {
    public static StoreResponse from(StoreResult result) {
        return new StoreResponse(
            result.storeId(),
            result.userId(),
            result.name(),
            result.commonInfo(),
            result.promptTemplate(),
            result.status()
        );
    }
}