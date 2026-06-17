package com.smartsub.guide.infrastructure;

import com.smartsub.guide.application.dto.ChatLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatLogProducer {

    private static final String TOPIC = "store-chat-logs";

    private final KafkaTemplate<String, ChatLogEvent> kafkaTemplate;

    public void send(ChatLogEvent event) {
        kafkaTemplate.send(TOPIC, event.storeId().toString(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("채팅 로그 이벤트 발행 실패: {}", event, ex);
                } else {
                    log.info("채팅 로그 이벤트 발행 성공: storeId={}", event.storeId());
                }
            });
    }
}
