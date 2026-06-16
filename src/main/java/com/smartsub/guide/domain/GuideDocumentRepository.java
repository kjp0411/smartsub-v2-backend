package com.smartsub.guide.domain;

import java.util.List;
import java.util.UUID;

public interface GuideDocumentRepository {

    void insertWithVector(UUID storeId, String content, String embedding);

    List<GuideDocumentProjection> findTopKBySimilarity(UUID storeId, String embedding, int topK);

    void deleteAllByStoreId(UUID storeId);
}
