package com.smartsub.guide.infrastructure;

import com.smartsub.guide.domain.ChatLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatLogJpaRepository extends JpaRepository<ChatLog, UUID> {

}
