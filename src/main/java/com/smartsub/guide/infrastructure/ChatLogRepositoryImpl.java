package com.smartsub.guide.infrastructure;

import com.smartsub.guide.domain.ChatCategoryCountProjection;
import com.smartsub.guide.domain.ChatLog;
import com.smartsub.guide.domain.ChatLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatLogRepositoryImpl implements ChatLogRepository {

    private final ChatLogJpaRepository chatLogJpaRepository;

    @Override
    public ChatLog save(ChatLog chatLog) {
        return chatLogJpaRepository.save(chatLog);
    }

    @Override
    public List<ChatCategoryCountProjection> countByStoreAndCategory(LocalDateTime start, LocalDateTime end) {
        return chatLogJpaRepository.countByStoreAndCategory(start, end);
    }
}
