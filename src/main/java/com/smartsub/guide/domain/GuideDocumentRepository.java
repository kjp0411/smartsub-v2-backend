package com.smartsub.guide.domain;

import java.util.List;
import java.util.UUID;

public interface GuideDocumentRepository {

    void insertWithVector(UUID storeId, String content, String embedding);

    List<GuideDocumentProjection> findTopKBySimilarity(UUID storeId, String embedding, int topK);

    List<GuideDocumentScoredProjection> findTopKBySimilarityWithScore(UUID storeId, String embedding, int topK);

    List<GuideDocumentProjection> findAllByStoreId(UUID storeId);

    void deleteAllByStoreId(UUID storeId);
}
