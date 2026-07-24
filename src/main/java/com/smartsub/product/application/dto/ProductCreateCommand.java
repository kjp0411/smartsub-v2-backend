package com.smartsub.product.application.dto;

import com.smartsub.product.domain.DescriptionSource;
import com.smartsub.product.domain.ProductStatus;
import com.smartsub.product.domain.ProductUnit;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductCreateCommand(
    UUID storeId,
    String name,
    String description,
    DescriptionSource descriptionSource,
    BigDecimal price,
    Integer stockQuantity,
    ProductUnit unit,
    ProductStatus status
) {
}
