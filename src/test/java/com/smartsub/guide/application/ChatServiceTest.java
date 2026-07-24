package com.smartsub.guide.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartsub.guide.application.dto.ChatCommand;
import com.smartsub.guide.application.dto.ChatLogEvent;
import com.smartsub.guide.application.dto.ChatResult;
import com.smartsub.guide.domain.GuideDocumentProjection;
import com.smartsub.guide.domain.GuideDocumentRepository;
import com.smartsub.guide.infrastructure.ChatLogProducer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatCompletionClient chatCompletionClient;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private GuideDocumentRepository guideDocumentRepository;

    @Mock
    private ChatLogProducer chatLogProducer;

    @Mock
    private HydeQueryExpander hydeQueryExpander;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        // mock MeterRegistry로는 Timer가 정상 동작하지 않아 SimpleMeterRegistry로 직접 생성 (@InjectMocks는 비-mock 필드를 채우지 못함)
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        chatService = new ChatService(
            chatCompletionClient,
            embeddingService,
            guideDocumentRepository,
            chatLogProducer,
            hydeQueryExpander,
            meterRegistry
        );
    }

    @Test
    @DisplayName("질문에 대해 유사 문서를 검색하고 AI 응답 및 토큰 사용량을 기록한다")
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

        when(hydeQueryExpander.expand(command.question()))
            .thenReturn("화장실 위치를 안내하는 답변입니다.");
        when(embeddingService.embedToString(anyString())).thenReturn("[0.1, 0.2, 0.3]");
        when(guideDocumentRepository.findTopKBySimilarity(storeId, "[0.1, 0.2, 0.3]", 3))
            .thenReturn(List.of(projection));

        String rawJson = """
            {"answer": "화장실은 1층 엘리베이터 옆에 있습니다.", "category": "FACILITY"}
            """;
        AssistantMessage assistantMessage = new AssistantMessage(rawJson);
        Generation generation = new Generation(assistantMessage);

        Usage usage = mock(Usage.class);
        when(usage.getPromptTokens()).thenReturn(420);
        when(usage.getCompletionTokens()).thenReturn(35);

        ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
        when(metadata.getUsage()).thenReturn(usage);

        ChatResponse chatResponse = mock(ChatResponse.class);
        when(chatResponse.getResult()).thenReturn(generation);
        when(chatResponse.getMetadata()).thenReturn(metadata);

        when(chatCompletionClient.complete(anyString())).thenReturn(chatResponse);

        // When
        ChatResult result = chatService.chat(command);

        // Then
        assertThat(result.answer()).isEqualTo("화장실은 1층 엘리베이터 옆에 있습니다.");
        assertThat(result.category()).isEqualTo("FACILITY");

        ArgumentCaptor<ChatLogEvent> eventCaptor = ArgumentCaptor.forClass(ChatLogEvent.class);
        verify(chatLogProducer).send(eventCaptor.capture());
        ChatLogEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.promptTokens()).isEqualTo(420);
        assertThat(capturedEvent.completionTokens()).isEqualTo(35);
        assertThat(capturedEvent.latencyMs()).isGreaterThanOrEqualTo(0);
    }
}