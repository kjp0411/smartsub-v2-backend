package com.smartsub.store.infrastructure;

import com.smartsub.store.domain.Store;
import com.smartsub.store.domain.StoreRepository;
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
    public List<Store> findAllByDeletedAtIsNull() {
        return storeJpaRepository.findAllByDeletedAtIsNull();
    }
}
