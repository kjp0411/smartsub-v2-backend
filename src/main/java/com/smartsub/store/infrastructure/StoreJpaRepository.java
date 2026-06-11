package com.smartsub.store.infrastructure;

import com.smartsub.store.domain.Store;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreJpaRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByIdAndDeletedAtIsNull(UUID storeId);

    List<Store> findAllByDeletedAtIsNull();
}
