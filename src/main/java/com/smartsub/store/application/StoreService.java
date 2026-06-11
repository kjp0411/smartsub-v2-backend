package com.smartsub.store.application;

import com.smartsub.store.application.dto.StoreCreateCommand;
import com.smartsub.store.application.dto.StoreResult;
import com.smartsub.store.application.dto.StoreUpdateCommand;
import com.smartsub.store.domain.Store;
import com.smartsub.store.domain.StoreRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private static final UUID SYSTEM_USER_ID =
        UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final StoreRepository storeRepository;

    @Transactional
    public StoreResult createStore(StoreCreateCommand command) {
        Store store = Store.create(
            command.userId(),
            command.name(),
            command.commonInfo(),
            command.promptTemplate(),
            command.status()
        );

        Store savedStore = storeRepository.save(store);

        return StoreResult.from(savedStore);
    }

    public List<StoreResult> getStores() {
        return storeRepository.findAllByDeletedAtIsNull()
            .stream()
            .map(StoreResult::from)
            .toList();
    }

    public StoreResult getStore(UUID storeId) {
        Store store = getActiveStore(storeId);

        return StoreResult.from(store);
    }

    @Transactional
    public StoreResult updateStore(UUID storeId, StoreUpdateCommand command) {
        Store store = getActiveStore(storeId);

        store.update(
            command.name(),
            command.commonInfo(),
            command.promptTemplate(),
            command.status()
        );

        return StoreResult.from(store);
    }

    @Transactional
    public void deleteStore(UUID storeId) {
        Store store = getActiveStore(storeId);

        store.delete(SYSTEM_USER_ID);
    }

    private Store getActiveStore(UUID storeId) {
        return storeRepository.findByIdAndDeletedAtIsNull(storeId)
            .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));
    }
}
