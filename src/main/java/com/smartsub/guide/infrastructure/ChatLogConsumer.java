package com.smartsub.guide.infrastructure;

import com.smartsub.guide.application.dto.ChatLogEvent;
import com.smartsub.guide.domain.ChatCategory;
import com.smartsub.guide.domain.ChatLog;
import com.smartsub.guide.domain.ChatLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatLogConsumer {

    private final ChatLogRepository chatLogRepository;

    @KafkaListener(topics = "store-chat-logs", groupId = "smartsub-chat-log-group")
    public void consume(ChatLogEvent event) {
        long start = System.currentTimeMillis();

        ChatCategory category = ChatCategory.from(event.category());

        ChatLog chatLog = ChatLog.create(
            event.storeId(),
            event.tableNumber(),
            event.question(),
            event.answer(),
            event.language(),
            category
        );
        chatLogRepository.save(chatLog);

        long elapsed = System.currentTimeMillis() - start;
        log.info("채팅 로그 비동기 저장 완료: storeId={}, 저장 시간={}ms", event.storeId(), elapsed);
    }
}
