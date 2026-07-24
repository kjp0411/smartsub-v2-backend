package com.smartsub.store.infrastructure;

import com.smartsub.store.domain.Store;
import com.smartsub.store.domain.StoreStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreJpaRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByIdAndDeletedAtIsNull(UUID storeId);

    Optional<Store> findByUserIdAndDeletedAtIsNull(UUID userId);

    List<Store> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    List<Store> findAllByDeletedAtIsNull();

    List<Store> findAllByStatusAndDeletedAtIsNull(StoreStatus status);
}
