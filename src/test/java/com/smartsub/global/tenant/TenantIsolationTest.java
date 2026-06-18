package com.smartsub.global.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartsub.product.domain.DescriptionSource;
import com.smartsub.product.domain.Product;
import com.smartsub.product.domain.ProductRepository;
import com.smartsub.product.domain.ProductStatus;
import com.smartsub.product.domain.ProductUnit;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
class TenantIsolationTest {

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("A 매장 컨텍스트에서 생성한 상품은 B 매장 컨텍스트에서 조회되지 않는다")
    @Transactional
    void productOfStoreA_isNotVisibleFromStoreB() {
        // Given
        UUID storeAId = UUID.randomUUID();
        UUID storeBId = UUID.randomUUID();

        TenantContext.setTenantId(storeAId);
        Product product = Product.create(
            storeAId, "A매장 김치찌개", "설명", DescriptionSource.MANUAL,
            BigDecimal.valueOf(9000), 10, ProductUnit.EA, ProductStatus.ON_SALE
        );
        productRepository.save(product);

        // When
        TenantContext.setTenantId(storeBId);
        List<Product> result = productRepository.findAllByDeletedAtIsNull();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("같은 매장 컨텍스트에서는 자신이 생성한 상품이 정상 조회된다")
    @Transactional
    void productOfStoreA_isVisibleFromStoreA() {
        // Given
        UUID storeAId = UUID.randomUUID();

        TenantContext.setTenantId(storeAId);
        Product product = Product.create(
            storeAId, "A매장 김치찌개", "설명", DescriptionSource.MANUAL,
            BigDecimal.valueOf(9000), 10, ProductUnit.EA, ProductStatus.ON_SALE
        );
        productRepository.save(product);

        // When
        List<Product> result = productRepository.findAllByDeletedAtIsNull();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("A매장 김치찌개");
    }
}
