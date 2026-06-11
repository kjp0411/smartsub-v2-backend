package com.smartsub.product.presentation.request;

import com.smartsub.product.application.dto.ProductCreateCommand;
import com.smartsub.product.domain.DescriptionSource;
import com.smartsub.product.domain.ProductStatus;
import com.smartsub.product.domain.ProductUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductCreateRequest(

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 100, message = "상품명은 최대 100자까지 입력할 수 있습니다.")
    String name,

    @Size(max = 1000, message = "상품 설명은 최대 1000자까지 입력할 수 있습니다.")
    String description,

    @NotNull(message = "상품 설명 출처는 필수입니다.")
    DescriptionSource descriptionSource,

    @NotNull(message = "상품 가격은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "상품 가격은 0보다 커야 합니다.")
    BigDecimal price,

    @NotNull(message = "상품 재고 수량은 필수입니다.")
    @Min(value = 0, message = "상품 재고 수량은 0 이상이어야 합니다.")
    Integer stockQuantity,

    @NotNull(message = "상품 단위는 필수입니다.")
    ProductUnit unit,

    @NotNull(message = "상품 상태는 필수입니다.")
    ProductStatus status
) {

    public ProductCreateCommand toCommand() {
        return new ProductCreateCommand(
            name,
            description,
            descriptionSource,
            price,
            stockQuantity,
            unit,
            status
        );
    }
}
