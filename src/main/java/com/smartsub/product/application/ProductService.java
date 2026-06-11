package com.smartsub.product.application;

import com.smartsub.product.application.dto.ProductCreateCommand;
import com.smartsub.product.application.dto.ProductResult;
import com.smartsub.product.application.dto.ProductUpdateCommand;
import com.smartsub.product.domain.Product;
import com.smartsub.product.domain.ProductRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final UUID SYSTEM_USER_ID =
        UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final ProductRepository productRepository;

    @Transactional
    public ProductResult createProduct(ProductCreateCommand command) {
        Product product = Product.create(
            command.name(),
            command.description(),
            command.descriptionSource(),
            command.price(),
            command.stockQuantity(),
            command.unit(),
            command.status()
        );

        Product savedProduct = productRepository.save(product);

        return ProductResult.from(savedProduct);
    }

    public List<ProductResult> getProducts() {
        return productRepository.findAllByDeletedAtIsNull()
            .stream()
            .map(ProductResult::from)
            .toList();
    }

    public ProductResult getProduct(UUID productId) {
        Product product = getActiveProduct(productId);

        return ProductResult.from(product);
    }

    @Transactional
    public ProductResult updateProduct(UUID productId, ProductUpdateCommand command) {
        Product product = getActiveProduct(productId);

        product.update(
            command.name(),
            command.description(),
            command.descriptionSource(),
            command.price(),
            command.stockQuantity(),
            command.unit(),
            command.status()
        );

        return ProductResult.from(product);
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        Product product = getActiveProduct(productId);

        product.delete(SYSTEM_USER_ID);
    }

    private Product getActiveProduct(UUID productId) {
        return productRepository.findByIdAndDeletedAtIsNull(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }
}
