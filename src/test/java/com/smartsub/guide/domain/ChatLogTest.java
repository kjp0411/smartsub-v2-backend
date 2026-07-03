package com.smartsub.guide.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChatLogTest {

    @Test
    @DisplayName("ChatLog.create()로 생성하면 입력한 필드가 정확히 설정된다")
    void create_setsFieldsCorrectly() {
        // Given
        UUID storeId = UUID.randomUUID();
        String tableNumber = "3";
        String question = "화장실이 어디에 있나요?";
        String answer = "화장실은 1층 엘리베이터 옆에 있습니다.";
        Language language = Language.KOREAN;
        ChatCategory category = ChatCategory.FACILITY;

        // When
        ChatLog chatLog = ChatLog.create(storeId, tableNumber, question, answer, language, category);

        // Then
        assertThat(chatLog.getStoreId()).isEqualTo(storeId);
        assertThat(chatLog.getTableNumber()).isEqualTo(tableNumber);
        assertThat(chatLog.getQuestion()).isEqualTo(question);
        assertThat(chatLog.getAnswer()).isEqualTo(answer);
        assertThat(chatLog.getLanguage()).isEqualTo(language);
        assertThat(chatLog.getCategory()).isEqualTo(category);
    }
}