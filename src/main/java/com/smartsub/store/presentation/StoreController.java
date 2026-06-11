package com.smartsub.store.presentation;

import com.smartsub.store.application.StoreService;
import com.smartsub.store.application.dto.StoreResult;
import com.smartsub.store.presentation.request.StoreCreateRequest;
import com.smartsub.store.presentation.request.StoreUpdateRequest;
import com.smartsub.store.presentation.response.StoreResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(
        @Valid @RequestBody StoreCreateRequest request
    ) {
        StoreResult result = storeService.createStore(request.toCommand());

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(StoreResponse.from(result));
    }

    @GetMapping
    public ResponseEntity<List<StoreResponse>> getStores() {
        List<StoreResponse> responses = storeService.getStores()
            .stream()
            .map(StoreResponse::from)
            .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> getStore(
        @PathVariable UUID storeId
    ) {
        StoreResult result = storeService.getStore(storeId);

        return ResponseEntity.ok(StoreResponse.from(result));
    }

    @PatchMapping("/{storeId}")
    public ResponseEntity<StoreResponse> updateStore(
        @PathVariable UUID storeId,
        @Valid @RequestBody StoreUpdateRequest request
    ) {
        StoreResult result = storeService.updateStore(storeId, request.toCommand());

        return ResponseEntity.ok(StoreResponse.from(result));
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(
        @PathVariable UUID storeId
    ) {
        storeService.deleteStore(storeId);

        return ResponseEntity.noContent().build();
    }
}
