package com.smartsub.product.presentation.response;

import com.smartsub.product.application.dto.ProductResult;
import com.smartsub.product.domain.DescriptionSource;
import com.smartsub.product.domain.ProductStatus;
import com.smartsub.product.domain.ProductUnit;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
    UUID productId,
    UUID storeId,
    String name,
    String description,
    DescriptionSource descriptionSource,
    BigDecimal price,
    Integer stockQuantity,
    ProductUnit unit,
    ProductStatus status
) {

    public static ProductResponse from(ProductResult result) {
        return new ProductResponse(
            result.productId(),
            result.storeId(),
            result.name(),
            result.description(),
            result.descriptionSource(),
            result.price(),
            result.stockQuantity(),
            result.unit(),
            result.status()
        );
    }
}
