package com.smartsub.guide.domain;

import java.util.UUID;

public interface ChatCategoryCountProjection {
    UUID getStoreId();
    ChatCategory getCategory();
    Long getCount();
}
