package com.smartsub.guide.infrastructure;

import com.smartsub.guide.domain.GuideDocumentProjection;
import com.smartsub.guide.domain.GuideDocumentRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GuideDocumentRepositoryImpl implements GuideDocumentRepository {

    private final GuideDocumentJpaRepository guideDocumentJpaRepository;

    @Override
    public void insertWithVector(UUID storeId, String content, String embedding) {
        guideDocumentJpaRepository.insertWithVector(storeId.toString(), content, embedding);
    }

    @Override
    public List<GuideDocumentProjection> findTopKBySimilarity(UUID storeId, String embedding, int topK) {
        return guideDocumentJpaRepository.findTopKBySimilarity(storeId.toString(), embedding, topK);
    }

    @Override
    public List<GuideDocumentProjection> findAllByStoreId(UUID storeId) {
        return guideDocumentJpaRepository.findAllByStoreId(storeId);
    }

    @Override
    public void deleteAllByStoreId(UUID storeId) {
        guideDocumentJpaRepository.deleteAllByStoreId(storeId);
    }
}