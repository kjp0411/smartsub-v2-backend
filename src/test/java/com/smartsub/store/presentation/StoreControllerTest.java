package com.smartsub.store.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsub.store.domain.StoreStatus;
import com.smartsub.store.presentation.request.StoreCreateRequest;
import com.smartsub.store.presentation.request.StoreUpdateRequest;
import com.smartsub.user.presentation.request.SignInRequest;
import com.smartsub.user.presentation.request.SignUpRequest;
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
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        String email = "store-test-" + UUID.randomUUID() + "@smartsub.com";
        SignUpRequest signUpRequest = new SignUpRequest(email, "password1234", "테스터");
        mockMvc.perform(post("/api/v1/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signUpRequest)));

        SignInRequest signInRequest = new SignInRequest(email, "password1234");
        String loginBody = mockMvc.perform(post("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)))
            .andReturn().getResponse().getContentAsString();

        accessToken = objectMapper.readTree(loginBody).get("accessToken").asText();
        userId = extractUserIdFromToken(accessToken);
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

    private StoreCreateRequest sampleCreateRequest() {
        return new StoreCreateRequest(
            userId, "테스트 매장", "영업시간은 11시부터입니다.",
            "당신은 매장의 AI 점장입니다.", StoreStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("매장 생성 성공 시 201 Created를 반환한다")
    @Transactional
    void createStore_success() throws Exception {
        // Given
        StoreCreateRequest request = sampleCreateRequest();

        // When & Then
        mockMvc.perform(post("/api/v1/stores")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("테스트 매장"));
    }

    @Test
    @DisplayName("인증 토큰 없이 매장 생성 시 403 Forbidden을 반환한다")
    @Transactional
    void createStore_withoutToken_returnsForbidden() throws Exception {
        // Given
        StoreCreateRequest request = sampleCreateRequest();

        // When & Then
        mockMvc.perform(post("/api/v1/stores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("매장 이름이 누락된 생성 요청은 400 Bad Request를 반환한다")
    @Transactional
    void createStore_missingName_returnsBadRequest() throws Exception {
        // Given
        String invalidJson = """
            {
                "userId": "%s",
                "commonInfo": "영업시간은 11시부터입니다.",
                "promptTemplate": "당신은 매장의 AI 점장입니다.",
                "status": "ACTIVE"
            }
            """.formatted(userId);

        // When & Then
        mockMvc.perform(post("/api/v1/stores")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("매장 목록 조회 성공 시 200 OK를 반환한다")
    @Transactional
    void getStores_success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/stores")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 매장 조회 시 404 Not Found를 반환한다")
    @Transactional
    void getStore_notFound_returnsNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/stores/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("STORE_NOT_FOUND"));
    }

    @Test
    @DisplayName("매장 생성 후 단건 조회 성공 시 200 OK를 반환한다")
    @Transactional
    void getStore_success() throws Exception {
        // Given
        String createResponseBody = mockMvc.perform(post("/api/v1/stores")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCreateRequest())))
            .andReturn().getResponse().getContentAsString();

        String storeId = objectMapper.readTree(createResponseBody).get("storeId").asText();

        // When & Then
        mockMvc.perform(get("/api/v1/stores/" + storeId)
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("테스트 매장"));
    }

    @Test
    @DisplayName("매장 수정 성공 시 200 OK를 반환한다")
    @Transactional
    void updateStore_success() throws Exception {
        // Given
        String createResponseBody = mockMvc.perform(post("/api/v1/stores")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCreateRequest())))
            .andReturn().getResponse().getContentAsString();

        String storeId = objectMapper.readTree(createResponseBody).get("storeId").asText();

        StoreUpdateRequest updateRequest = new StoreUpdateRequest(
            "수정된 매장명", "수정된 안내사항", "수정된 프롬프트 템플릿", StoreStatus.PAUSED
        );

        // When & Then
        mockMvc.perform(patch("/api/v1/stores/" + storeId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("수정된 매장명"))
            .andExpect(jsonPath("$.status").value("PAUSED"));
    }

    @Test
    @DisplayName("매장 삭제 성공 시 204 No Content를 반환한다")
    @Transactional
    void deleteStore_success() throws Exception {
        // Given
        String createResponseBody = mockMvc.perform(post("/api/v1/stores")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCreateRequest())))
            .andReturn().getResponse().getContentAsString();

        String storeId = objectMapper.readTree(createResponseBody).get("storeId").asText();

        // When & Then
        mockMvc.perform(delete("/api/v1/stores/" + storeId)
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNoContent());
    }
}