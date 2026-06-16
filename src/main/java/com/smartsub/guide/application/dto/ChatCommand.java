package com.smartsub.guide.application.dto;

import java.util.UUID;

public record ChatCommand(
    UUID storeId,
    String tableNumber,
    String question
) {
}
