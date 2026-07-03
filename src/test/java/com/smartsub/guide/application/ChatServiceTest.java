package com.smartsub.guide.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartsub.guide.application.dto.ChatCommand;
import com.smartsub.guide.application.dto.ChatLogEvent;
import com.smartsub.guide.application.dto.ChatResult;
import com.smartsub.guide.domain.GuideDocumentProjection;
import com.smartsub.guide.domain.GuideDocumentRepository;
import com.smartsub.guide.infrastructure.ChatLogProducer;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private GuideDocumentRepository guideDocumentRepository;

    @Mock
    private ChatLogProducer chatLogProducer;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @InjectMocks
    private ChatService chatService;

    @Test
    @DisplayName("질문에 대해 유사 문서를 검색하고 AI 응답을 생성한다")
    void chat_success() {
        // Given
        UUID storeId = UUID.randomUUID();
        ChatCommand command = new ChatCommand(storeId, "3", "화장실이 어디에 있나요?");

        GuideDocumentProjection projection = new GuideDocumentProjection() {
            @Override
            public String getId() { return UUID.randomUUID().toString(); }
            @Override
            public String getStoreId() { return storeId.toString(); }
            @Override
            public String getContent() { return "화장실은 1층 엘리베이터 옆에 있습니다."; }
        };

        when(embeddingService.embedToString(command.question())).thenReturn("[0.1, 0.2, 0.3]");
        when(guideDocumentRepository.findTopKBySimilarity(storeId, "[0.1, 0.2, 0.3]", 3))
            .thenReturn(List.of(projection));

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(ChatService.LlmChatResponse.class))
            .thenReturn(new ChatService.LlmChatResponse(
                "화장실은 1층 엘리베이터 옆에 있습니다.", "FACILITY"));

        // When
        ChatResult result = chatService.chat(command);

        // Then
        assertThat(result.answer()).isEqualTo("화장실은 1층 엘리베이터 옆에 있습니다.");
        assertThat(result.category()).isEqualTo("FACILITY");
        verify(chatLogProducer).send(any(ChatLogEvent.class));
    }
}