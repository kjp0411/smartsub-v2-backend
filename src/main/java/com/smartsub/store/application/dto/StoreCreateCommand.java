package com.smartsub.store.application.dto;

import com.smartsub.store.domain.StoreStatus;
import java.util.UUID;

public record StoreCreateCommand(
    UUID userId,
    String name,
    String commonInfo,
    String promptTemplate,
    StoreStatus status
) {
}