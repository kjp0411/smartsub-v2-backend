package com.smartsub.guide.domain;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatLogRepository {

    ChatLog save(ChatLog chatLog);

    List<ChatCategoryCountProjection> countByStoreAndCategory(LocalDateTime start, LocalDateTime end);
}
