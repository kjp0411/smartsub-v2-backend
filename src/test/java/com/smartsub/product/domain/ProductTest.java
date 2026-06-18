package com.smartsub.product.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    @DisplayName("Product.create()로 생성하면 입력한 필드가 정확히 설정된다")
    void create_setsFieldsCorrectly() {
        // Given
        UUID storeId = UUID.randomUUID();
        String name = "김치찌개";
        String description = "직접 담근 김치로 만든 찌개";
        DescriptionSource descriptionSource = DescriptionSource.MANUAL;
        BigDecimal price = BigDecimal.valueOf(9000);
        Integer stockQuantity = 100;
        ProductUnit unit = ProductUnit.EA;
        ProductStatus status = ProductStatus.ON_SALE;

        // When
        Product product = Product.create(
            storeId, name, description, descriptionSource, price, stockQuantity, unit, status
        );

        // Then
        assertThat(product.getStoreId()).isEqualTo(storeId);
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getDescription()).isEqualTo(description);
        assertThat(product.getDescriptionSource()).isEqualTo(descriptionSource);
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getStockQuantity()).isEqualTo(stockQuantity);
        assertThat(product.getUnit()).isEqualTo(unit);
        assertThat(product.getStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("Product.update()로 수정하면 필드가 정확히 변경된다")
    void update_changesFieldsCorrectly() {
        // Given
        Product product = Product.create(
            UUID.randomUUID(), "기존 상품", "기존 설명", DescriptionSource.MANUAL,
            BigDecimal.valueOf(5000), 50, ProductUnit.EA, ProductStatus.ON_SALE
        );

        String updatedName = "변경된 상품";
        String updatedDescription = "변경된 설명";
        DescriptionSource updatedSource = DescriptionSource.AI_GENERATED;
        BigDecimal updatedPrice = BigDecimal.valueOf(7000);
        Integer updatedStock = 30;
        ProductUnit updatedUnit = ProductUnit.KG;
        ProductStatus updatedStatus = ProductStatus.OUT_OF_STOCK;

        // When
        product.update(
            updatedName, updatedDescription, updatedSource, updatedPrice, updatedStock, updatedUnit, updatedStatus
        );

        // Then
        assertThat(product.getName()).isEqualTo(updatedName);
        assertThat(product.getDescription()).isEqualTo(updatedDescription);
        assertThat(product.getDescriptionSource()).isEqualTo(updatedSource);
        assertThat(product.getPrice()).isEqualTo(updatedPrice);
        assertThat(product.getStockQuantity()).isEqualTo(updatedStock);
        assertThat(product.getUnit()).isEqualTo(updatedUnit);
        assertThat(product.getStatus()).isEqualTo(updatedStatus);
    }
}