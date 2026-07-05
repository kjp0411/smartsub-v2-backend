package com.smartsub.store.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository {

    Store save(Store store);

    Optional<Store> findByIdAndDeletedAtIsNull(UUID storeId);

    Optional<Store> findByUserIdAndDeletedAtIsNull(UUID userId);

    List<Store> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    List<Store> findAllByDeletedAtIsNull();

    List<Store> findAllByStatusAndDeletedAtIsNull(StoreStatus status);
}
