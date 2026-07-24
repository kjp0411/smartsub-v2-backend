package com.smartsub.store.application;

import com.smartsub.global.exception.BusinessException;
import com.smartsub.global.exception.ErrorCode;
import com.smartsub.store.application.dto.StoreCreateCommand;
import com.smartsub.store.application.dto.StoreResult;
import com.smartsub.store.application.dto.StoreUpdateCommand;
import com.smartsub.store.domain.Store;
import com.smartsub.store.domain.StoreRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional
    public StoreResult createStore(StoreCreateCommand command) {
        boolean alreadyHasStore = !storeRepository
            .findAllByUserIdAndDeletedAtIsNull(command.userId())
            .isEmpty();

        if (alreadyHasStore) {
            throw new BusinessException(ErrorCode.STORE_ALREADY_EXISTS);
        }

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
        UUID userId = currentUserId();

        return storeRepository.findAllByUserIdAndDeletedAtIsNull(userId)
            .stream()
            .map(StoreResult::from)
            .toList();
    }

    public StoreResult getStore(UUID storeId) {
        Store store = getOwnedStore(storeId);

        return StoreResult.from(store);
    }

    @Transactional
    public StoreResult updateStore(UUID storeId, StoreUpdateCommand command) {
        Store store = getOwnedStore(storeId);

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
        Store store = getOwnedStore(storeId);
        UUID userId = currentUserId();

        store.delete(userId);
    }

    private Store getOwnedStore(UUID storeId) {
        UUID userId = currentUserId();

        Store store = storeRepository.findByIdAndDeletedAtIsNull(storeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        if (!store.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }

        return store;
    }

    private UUID currentUserId() {
        return (UUID) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
    }
}
