package com.smartsub.guide.application;

import com.smartsub.guide.domain.GuideDocumentRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final GuideDocumentRepository guideDocumentRepository;

    @Transactional
    public void embedAndSave(UUID storeId, List<String> texts) {
        guideDocumentRepository.deleteAllByStoreId(storeId);

        for (String text : texts) {
            float[] embedding = embeddingModel.embed(text);
            String embeddingStr = toVectorString(embedding);
            guideDocumentRepository.insertWithVector(storeId, text, embeddingStr);
        }
    }

    public String embedToString(String text) {
        return toVectorString(embeddingModel.embed(text));
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