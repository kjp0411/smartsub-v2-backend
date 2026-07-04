package com.smartsub.store.infrastructure;

import com.smartsub.store.domain.Store;
import com.smartsub.store.domain.StoreRepository;
import com.smartsub.store.domain.StoreStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepository {

    private final StoreJpaRepository storeJpaRepository;

    @Override
    public Store save(Store store) {
        return storeJpaRepository.save(store);
    }

    @Override
    public Optional<Store> findByIdAndDeletedAtIsNull(UUID storeId) {
        return storeJpaRepository.findByIdAndDeletedAtIsNull(storeId);
    }

    @Override
    public Optional<Store> findByUserIdAndDeletedAtIsNull(UUID userId) {
        return storeJpaRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public List<Store> findAllByDeletedAtIsNull() {
        return storeJpaRepository.findAllByDeletedAtIsNull();
    }

    @Override
    public List<Store> findAllByStatusAndDeletedAtIsNull(StoreStatus status) {
        return storeJpaRepository.findAllByStatusAndDeletedAtIsNull(status);
    }
}
