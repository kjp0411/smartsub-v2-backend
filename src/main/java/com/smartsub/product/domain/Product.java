package com.smartsub.product.domain;

import com.smartsub.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "description_source", nullable = false)
    private DescriptionSource descriptionSource;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false)
    private ProductUnit unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status;

    private Product(
        String name,
        String description,
        DescriptionSource descriptionSource,
        BigDecimal price,
        Integer stockQuantity,
        ProductUnit unit,
        ProductStatus status
    ) {
        this.name = name;
        this.description = description;
        this.descriptionSource = descriptionSource;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.unit = unit;
        this.status = status;
    }

    public static Product create(
        String name,
        String description,
        DescriptionSource descriptionSource,
        BigDecimal price,
        Integer stockQuantity,
        ProductUnit unit,
        ProductStatus status
    ) {
        return new Product(name, description, descriptionSource, price, stockQuantity, unit, status);
    }

    public void update(
        String name,
        String description,
        DescriptionSource descriptionSource,
        BigDecimal price,
        Integer stockQuantity,
        ProductUnit unit,
        ProductStatus status
    ) {
        this.name = name;
        this.description = description;
        this.descriptionSource = descriptionSource;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.unit = unit;
        this.status = status;
    }
}
