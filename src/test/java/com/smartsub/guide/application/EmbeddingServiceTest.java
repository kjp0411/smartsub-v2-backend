package com.smartsub.guide.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartsub.guide.domain.GuideDocumentRepository;
import com.smartsub.store.domain.Store;
import com.smartsub.store.domain.StoreRepository;
import com.smartsub.store.domain.StoreStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private GuideDocumentRepository guideDocumentRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private EmbeddingService embeddingService;

    private UUID userId;

    @BeforeEach
    void setUpSecurityContext() {
        userId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("가이드 텍스트를 임베딩하여 저장하면 기존 데이터를 삭제 후 새로 저장한다")
    void embedAndSave_success() {
        // Given
        UUID storeId = UUID.randomUUID();
        List<String> texts = List.of("화장실은 1층에 있습니다.", "주차는 무료입니다.");
        float[] mockEmbedding = new float[]{0.1f, 0.2f, 0.3f};

        Store store = Store.create(userId, "테스트매장", null, "prompt", StoreStatus.ACTIVE);
        when(storeRepository.findByIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));
        when(embeddingModel.embed(anyString())).thenReturn(mockEmbedding);

        // When
        embeddingService.embedAndSave(storeId, texts);

        // Then
        verify(guideDocumentRepository).deleteAllByStoreId(storeId);
        verify(guideDocumentRepository, org.mockito.Mockito.times(2))
            .insertWithVector(any(UUID.class), anyString(), anyString());
    }

    @Test
    @DisplayName("텍스트를 임베딩하면 벡터 문자열 형식으로 변환된다")
    void embedToString_success() {
        // Given
        String text = "화장실이 어디에 있나요?";
        float[] mockEmbedding = new float[]{0.1f, 0.2f, 0.3f};

        when(embeddingModel.embed(text)).thenReturn(mockEmbedding);

        // When
        String result = embeddingService.embedToString(text);

        // Then
        assertThat(result).isEqualTo("[0.1,0.2,0.3]");
    }
}