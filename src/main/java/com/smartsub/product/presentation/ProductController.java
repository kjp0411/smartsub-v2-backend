package com.smartsub.product.presentation;

import com.smartsub.product.application.ProductService;
import com.smartsub.product.application.dto.ProductResult;
import com.smartsub.product.presentation.request.ProductCreateRequest;
import com.smartsub.product.presentation.request.ProductUpdateRequest;
import com.smartsub.product.presentation.response.ProductResponse;
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
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
        @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResult result = productService.createProduct(request.toCommand());

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ProductResponse.from(result));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts() {
        List<ProductResponse> responses = productService.getProducts()
            .stream()
            .map(ProductResponse::from)
            .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(
        @PathVariable UUID productId
    ) {
        ProductResult result = productService.getProduct(productId);

        return ResponseEntity.ok(ProductResponse.from(result));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
        @PathVariable UUID productId,
        @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductResult result = productService.updateProduct(productId, request.toCommand());

        return ResponseEntity.ok(ProductResponse.from(result));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
        @PathVariable UUID productId
    ) {
        productService.deleteProduct(productId);

        return ResponseEntity.noContent().build();
    }
}
