package com.smartsub.guide.infrastructure;

import com.smartsub.guide.domain.ChatLog;
import com.smartsub.guide.domain.ChatLogRepository;
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
}
