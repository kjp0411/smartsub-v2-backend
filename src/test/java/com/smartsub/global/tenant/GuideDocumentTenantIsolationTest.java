package com.smartsub.global.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartsub.guide.domain.GuideDocumentProjection;
import com.smartsub.guide.domain.GuideDocumentRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
class GuideDocumentTenantIsolationTest {

    @Autowired
    private GuideDocumentRepository guideDocumentRepository;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private String randomEmbedding() {
        return "[" + IntStream.range(0, 1536)
            .mapToObj(i -> String.valueOf(Math.random()))
            .collect(Collectors.joining(",")) + "]";
    }

    @Test
    @DisplayName("A 매장의 가이드 문서는 B 매장의 유사도 검색 결과에 포함되지 않는다")
    @Transactional
    void guideDocumentOfStoreA_isNotVisibleFromStoreB() {
        // Given
        UUID storeAId = UUID.randomUUID();
        UUID storeBId = UUID.randomUUID();
        String embedding = randomEmbedding();

        guideDocumentRepository.insertWithVector(storeAId, "A매장 영업시간은 11시부터입니다.", embedding);

        // When
        List<GuideDocumentProjection> result =
            guideDocumentRepository.findTopKBySimilarity(storeBId, embedding, 3);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("같은 매장의 유사도 검색에서는 자신이 등록한 가이드 문서가 조회된다")
    @Transactional
    void guideDocumentOfStoreA_isVisibleFromStoreA() {
        // Given
        UUID storeAId = UUID.randomUUID();
        String embedding = randomEmbedding();

        guideDocumentRepository.insertWithVector(storeAId, "A매장 영업시간은 11시부터입니다.", embedding);

        // When
        List<GuideDocumentProjection> result =
            guideDocumentRepository.findTopKBySimilarity(storeAId, embedding, 3);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("A매장 영업시간은 11시부터입니다.");
    }
}