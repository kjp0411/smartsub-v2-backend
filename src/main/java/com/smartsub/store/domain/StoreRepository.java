package com.smartsub.store.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository {

    Store save(Store store);

    Optional<Store> findByIdAndDeletedAtIsNull(UUID storeId);

    List<Store> findAllByDeletedAtIsNull();
}
