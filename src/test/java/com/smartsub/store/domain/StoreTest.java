package com.smartsub.store.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StoreTest {

    @Test
    @DisplayName("Store.create()로 생성하면 입력한 필드가 정확히 설정된다")
    void create_setsFieldsCorrectly() {
        // Given
        UUID userId = UUID.randomUUID();
        String name = "스마트섭 식당";
        String commonInfo = "영업시간 11:00 ~ 22:00";
        String promptTemplate = "당신은 매장의 AI 점장입니다.";
        StoreStatus status = StoreStatus.ACTIVE;

        // When
        Store store = Store.create(userId, name, commonInfo, promptTemplate, status);

        // Then
        assertThat(store.getUserId()).isEqualTo(userId);
        assertThat(store.getName()).isEqualTo(name);
        assertThat(store.getCommonInfo()).isEqualTo(commonInfo);
        assertThat(store.getPromptTemplate()).isEqualTo(promptTemplate);
        assertThat(store.getStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("Store.update()로 수정하면 필드가 정확히 변경된다")
    void update_changesFieldsCorrectly() {
        // Given
        Store store = Store.create(
            UUID.randomUUID(), "기존 매장명", "기존 정보", "기존 템플릿", StoreStatus.ACTIVE
        );

        String updatedName = "변경된 매장명";
        String updatedCommonInfo = "변경된 정보";
        String updatedPromptTemplate = "변경된 템플릿";
        StoreStatus updatedStatus = StoreStatus.PAUSED;

        // When
        store.update(updatedName, updatedCommonInfo, updatedPromptTemplate, updatedStatus);

        // Then
        assertThat(store.getName()).isEqualTo(updatedName);
        assertThat(store.getCommonInfo()).isEqualTo(updatedCommonInfo);
        assertThat(store.getPromptTemplate()).isEqualTo(updatedPromptTemplate);
        assertThat(store.getStatus()).isEqualTo(updatedStatus);
    }
}