package com.smartsub.guide.infrastructure;

import com.smartsub.guide.domain.ChatCategoryCountProjection;
import com.smartsub.guide.domain.ChatLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatLogJpaRepository extends JpaRepository<ChatLog, UUID> {

    @Query("""
        SELECT c.storeId AS storeId, c.category AS category, COUNT(c) AS count
        FROM ChatLog c
        WHERE c.createdAt >= :start AND c.createdAt < :end
          AND c.category IS NOT NULL
        GROUP BY c.storeId, c.category
        """)
    List<ChatCategoryCountProjection> countByStoreAndCategory(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
