package com.smartsub.guide.infrastructure;

import com.smartsub.guide.domain.GuideDocument;
import com.smartsub.guide.domain.GuideDocumentProjection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GuideDocumentJpaRepository extends JpaRepository<GuideDocument, UUID> {

    @Modifying
    @Query(value = """
        INSERT INTO p_guide_documents (id, store_id, content, embedding)
        VALUES (gen_random_uuid(), CAST(:storeId AS uuid), :content, CAST(:embedding AS vector))
        """, nativeQuery = true)
    void insertWithVector(
        @Param("storeId") String storeId,
        @Param("content") String content,
        @Param("embedding") String embedding
    );

    @Query(value = """
        SELECT id, store_id, content
        FROM p_guide_documents
        WHERE store_id = CAST(:storeId AS uuid)
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :topK
        """, nativeQuery = true)
    List<GuideDocumentProjection> findTopKBySimilarity(
        @Param("storeId") String storeId,
        @Param("embedding") String embedding,
        @Param("topK") int topK
    );

    void deleteAllByStoreId(UUID storeId);
}