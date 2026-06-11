package com.smartsub.store.application.dto;

import com.smartsub.store.domain.StoreStatus;

public record StoreUpdateCommand(
    String name,
    String commonInfo,
    String promptTemplate,
    StoreStatus status
) {
}