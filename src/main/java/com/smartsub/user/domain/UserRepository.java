package com.smartsub.user.domain;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndDeletedAtIsNull(UUID userid);

    boolean existsByEmail(String email);
}
