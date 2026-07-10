package com.smartsub.guide.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

@ExtendWith(MockitoExtension.class)
class HydeQueryExpanderTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @InjectMocks
    private HydeQueryExpander hydeQueryExpander;

    @Test
    @DisplayName("LLM 호출에 성공하면 가상 답변을 반환한다")
    void expand_success() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("화장실은 입구 오른쪽에 위치합니다.");

        String result = hydeQueryExpander.expand("화장실이 어디예요?");

        assertThat(result).isEqualTo("화장실은 입구 오른쪽에 위치합니다.");
    }

    @Test
    @DisplayName("LLM 호출이 실패하면 원본 질문으로 폴백한다")
    void expand_fallbackOnFailure() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("OpenAI timeout"));

        String result = hydeQueryExpander.expand("화장실이 어디예요?");

        assertThat(result).isEqualTo("화장실이 어디예요?");
    }
}