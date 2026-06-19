package com.smartsub.guide.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsub.guide.application.ChatService;
import com.smartsub.guide.application.EmbeddingService;
import com.smartsub.guide.application.dto.ChatResult;
import com.smartsub.guide.presentation.request.ChatRequest;
import com.smartsub.guide.presentation.request.GuideEmbedRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GuideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private EmbeddingService embeddingService;

    @Test
    @DisplayName("가이드 텍스트 임베딩 등록 성공 시 200 OK를 반환한다")
    void embed_success() throws Exception {
        // Given
        GuideEmbedRequest request = new GuideEmbedRequest(
            UUID.randomUUID(),
            List.of("영업시간은 11시부터입니다.", "주차는 건물 뒤편에 가능합니다.")
        );

        // When & Then
        mockMvc.perform(post("/api/v1/guide/embed")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(embeddingService).embedAndSave(request.storeId(), request.texts());
    }

    @Test
    @DisplayName("매장 ID가 누락된 임베딩 요청은 400 Bad Request를 반환한다")
    void embed_missingStoreId_returnsBadRequest() throws Exception {
        // Given
        String invalidJson = """
            {
                "texts": ["영업시간은 11시부터입니다."]
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/guide/embed")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("빈 텍스트 목록으로 임베딩 요청 시 400 Bad Request를 반환한다")
    void embed_emptyTexts_returnsBadRequest() throws Exception {
        // Given
        GuideEmbedRequest request = new GuideEmbedRequest(UUID.randomUUID(), List.of());

        // When & Then
        mockMvc.perform(post("/api/v1/guide/embed")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("챗봇 질문 성공 시 200 OK와 answer를 반환한다")
    void chat_success() throws Exception {
        // Given
        ChatRequest request = new ChatRequest(UUID.randomUUID(), "3", "영업시간이 언제예요?");
        ChatResult mockResult = new ChatResult("영업시간은 11시부터 22시까지입니다.");

        when(chatService.chat(any())).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/v1/guide/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.answer").value("영업시간은 11시부터 22시까지입니다."));
    }

    @Test
    @DisplayName("질문이 비어있는 채팅 요청은 400 Bad Request를 반환한다")
    void chat_blankQuestion_returnsBadRequest() throws Exception {
        // Given
        String invalidJson = """
            {
                "storeId": "%s",
                "tableNumber": "3",
                "question": ""
            }
            """.formatted(UUID.randomUUID());

        // When & Then
        mockMvc.perform(post("/api/v1/guide/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }
}