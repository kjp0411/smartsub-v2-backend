package com.smartsub.product.application.dto;

import com.smartsub.product.domain.DescriptionSource;
import com.smartsub.product.domain.Product;
import com.smartsub.product.domain.ProductStatus;
import com.smartsub.product.domain.ProductUnit;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductResult(
    UUID productId,
    String name,
    String description,
    DescriptionSource descriptionSource,
    BigDecimal price,
    Integer stockQuantity,
    ProductUnit unit,
    ProductStatus status
) {

    public static ProductResult from(Product product) {
        return new ProductResult(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getDescriptionSource(),
            product.getPrice(),
            product.getStockQuantity(),
            product.getUnit(),
            product.getStatus()
        );
    }
}
