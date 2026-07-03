package com.smartsub.guide.application.dto;

import com.smartsub.guide.domain.Language;
import java.util.UUID;

public record ChatLogEvent(
   UUID storeId,
   String tableNumber,
   String question,
   String answer,
   Language language,
   String category
) {
}
