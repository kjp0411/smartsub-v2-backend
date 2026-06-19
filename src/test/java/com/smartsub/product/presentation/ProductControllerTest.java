package com.smartsub.product.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsub.product.domain.DescriptionSource;
import com.smartsub.product.domain.ProductStatus;
import com.smartsub.product.domain.ProductUnit;
import com.smartsub.product.presentation.request.ProductCreateRequest;
import com.smartsub.product.presentation.request.ProductUpdateRequest;
import com.smartsub.store.domain.StoreStatus;
import com.smartsub.store.presentation.request.StoreCreateRequest;
import com.smartsub.user.presentation.request.SignInRequest;
import com.smartsub.user.presentation.request.SignUpRequest;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;
    private UUID storeId;

    @BeforeEach
    void setUp() throws Exception {
        String email = "product-test-" + UUID.randomUUID() + "@smartsub.com";
        SignUpRequest signUpRequest = new SignUpRequest(email, "password1234", "테스터");
        mockMvc.perform(post("/api/v1/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signUpRequest)));

        SignInRequest signInRequest = new SignInRequest(email, "password1234");

        String firstLoginBody = mockMvc.perform(post("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)))
            .andReturn().getResponse().getContentAsString();
        String firstToken = objectMapper.readTree(firstLoginBody).get("accessToken").asText();

        UUID userId = extractUserIdFromToken(firstToken);
        StoreCreateRequest storeCreateRequest = new StoreCreateRequest(
            userId, "테스트 매장", "영업시간 11시부터", "당신은 매장의 AI 점장입니다.", StoreStatus.ACTIVE
        );
        String storeResponseBody = mockMvc.perform(post("/api/v1/stores")
                .header("Authorization", "Bearer " + firstToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(storeCreateRequest)))
            .andReturn().getResponse().getContentAsString();

        storeId = UUID.fromString(objectMapper.readTree(storeResponseBody).get("storeId").asText());

        String secondLoginBody = mockMvc.perform(post("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)))
            .andReturn().getResponse().getContentAsString();
        accessToken = objectMapper.readTree(secondLoginBody).get("accessToken").asText();
    }

    private UUID extractUserIdFromToken(String token) {
        String[] parts = token.split("\\.");
        String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
        try {
            return UUID.fromString(objectMapper.readTree(payloadJson).get("sub").asText());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ProductCreateRequest sampleCreateRequest(UUID storeId) {
        return new ProductCreateRequest(
            storeId, "김치찌개", "직접 담근 김치로 만든 찌개",
            DescriptionSource.MANUAL, BigDecimal.valueOf(9000), 10,
            ProductUnit.EA, ProductStatus.ON_SALE
        );
    }

    @Test
    @DisplayName("상품 생성 성공 시 201 Created를 반환한다")
    @Transactional
    void createProduct_success() throws Exception {
        // Given
        ProductCreateRequest request = sampleCreateRequest(storeId);

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("김치찌개"));
    }

    @Test
    @DisplayName("인증 토큰 없이 상품 생성 시 403 Forbidden을 반환한다")
    @Transactional
    void createProduct_withoutToken_returnsForbidden() throws Exception {
        // Given
        ProductCreateRequest request = sampleCreateRequest(storeId);

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("가격이 0 이하인 상품 생성 요청은 400 Bad Request를 반환한다")
    @Transactional
    void createProduct_invalidPrice_returnsBadRequest() throws Exception {
        // Given
        String invalidJson = """
            {
                "storeId": "%s",
                "name": "김치찌개",
                "descriptionSource": "MANUAL",
                "price": 0,
                "stockQuantity": 10,
                "unit": "EA",
                "status": "ON_SALE"
            }
            """.formatted(storeId);

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("상품 목록 조회 성공 시 200 OK를 반환한다")
    @Transactional
    void getProducts_success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/products")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 시 404 Not Found를 반환한다")
    @Transactional
    void getProduct_notFound_returnsNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/products/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    @DisplayName("상품 생성 후 단건 조회 성공 시 200 OK를 반환한다")
    @Transactional
    void getProduct_success() throws Exception {
        // Given
        String createResponseBody = mockMvc.perform(post("/api/v1/products")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCreateRequest(storeId))))
            .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(createResponseBody).get("productId").asText();

        // When & Then
        mockMvc.perform(get("/api/v1/products/" + productId)
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("김치찌개"));
    }

    @Test
    @DisplayName("상품 수정 성공 시 200 OK를 반환한다")
    @Transactional
    void updateProduct_success() throws Exception {
        // Given
        String createResponseBody = mockMvc.perform(post("/api/v1/products")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCreateRequest(storeId))))
            .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(createResponseBody).get("productId").asText();

        ProductUpdateRequest updateRequest = new ProductUpdateRequest(
            "수정된 김치찌개", "수정된 설명", DescriptionSource.AI_GENERATED,
            BigDecimal.valueOf(11000), 5, ProductUnit.EA, ProductStatus.OUT_OF_STOCK
        );

        // When & Then
        mockMvc.perform(patch("/api/v1/products/" + productId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("수정된 김치찌개"))
            .andExpect(jsonPath("$.status").value("OUT_OF_STOCK"));
    }

    @Test
    @DisplayName("상품 삭제 성공 시 204 No Content를 반환한다")
    @Transactional
    void deleteProduct_success() throws Exception {
        // Given
        String createResponseBody = mockMvc.perform(post("/api/v1/products")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCreateRequest(storeId))))
            .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(createResponseBody).get("productId").asText();

        // When & Then
        mockMvc.perform(delete("/api/v1/products/" + productId)
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNoContent());
    }
}