package com.smartsub.guide.application;

import com.smartsub.global.exception.BusinessException;
import com.smartsub.global.exception.ErrorCode;
import com.smartsub.guide.domain.GuideDocumentProjection;
import com.smartsub.guide.domain.GuideDocumentRepository;
import com.smartsub.store.domain.StoreRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final GuideDocumentRepository guideDocumentRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public void embedAndSave(UUID storeId, List<String> texts) {
        validateOwnership(storeId);

        guideDocumentRepository.deleteAllByStoreId(storeId);

        for (String text : texts) {
            float[] embedding = embeddingModel.embed(text);
            String embeddingStr = toVectorString(embedding);
            guideDocumentRepository.insertWithVector(storeId, text, embeddingStr);
        }
    }

    public List<GuideDocumentProjection> getDocuments(UUID storeId) {
        validateOwnership(storeId);

        return guideDocumentRepository.findAllByStoreId(storeId);
    }

    public String embedToString(String text) {
        return toVectorString(embeddingModel.embed(text));
    }

    private void validateOwnership(UUID storeId) {
        UUID userId = (UUID) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();

        storeRepository.findByIdAndDeletedAtIsNull(storeId)
            .filter(store -> store.getUserId().equals(userId))
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
    }

    private String toVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}