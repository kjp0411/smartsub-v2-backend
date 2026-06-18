package com.smartsub.guide.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GuideDocumentTest {

    @Test
    @DisplayName("GuideDocument.create()로 생성하면 입력한 필드가 정확히 설정된다")
    void create_setsFieldsCorrectly() {
        // Given
        UUID storeId = UUID.randomUUID();
        String content = "화장실은 1층 엘리베이터 옆에 있습니다.";
        float[] embedding = new float[]{0.1f, 0.2f, 0.3f};

        // When
        GuideDocument guideDocument = GuideDocument.create(storeId, content, embedding);

        // Then
        assertThat(guideDocument.getStoreId()).isEqualTo(storeId);
        assertThat(guideDocument.getContent()).isEqualTo(content);
        assertThat(guideDocument.getEmbedding()).isEqualTo(embedding);
    }
}