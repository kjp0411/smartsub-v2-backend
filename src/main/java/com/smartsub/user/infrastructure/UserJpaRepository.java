package com.smartsub.user.infrastructure;

import com.smartsub.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndDeletedAtIsNull(UUID userId);

    boolean existsByEmail(String email);
}
