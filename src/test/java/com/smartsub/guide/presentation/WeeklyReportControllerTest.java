package com.smartsub.guide.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsub.guide.domain.ChatCategory;
import com.smartsub.guide.domain.WeeklyReport;
import com.smartsub.guide.domain.WeeklyReportRepository;
import com.smartsub.store.domain.StoreStatus;
import com.smartsub.store.presentation.request.StoreCreateRequest;
import com.smartsub.user.presentation.request.SignInRequest;
import com.smartsub.user.presentation.request.SignUpRequest;
import java.time.LocalDateTime;
import java.util.Map;
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
@Transactional
class WeeklyReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WeeklyReportRepository weeklyReportRepository;

    private String accessToken;
    private UUID storeId;

    private String otherAccessToken;
    private UUID otherStoreId;

    @BeforeEach
    void setUp() throws Exception {
        // 매장 A (테스트 주체)
        String email = "report-test-" + UUID.randomUUID() + "@smartsub.com";
        SignUpRequest signUpRequest = new SignUpRequest(email, "password1234", "테스터A");
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
            userId, "테스트 매장A", "영업시간 11시부터", "당신은 매장의 AI 점장입니다.", StoreStatus.ACTIVE
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

        // 매장 B (격리 검증용 타 매장)
        String otherEmail = "report-test-other-" + UUID.randomUUID() + "@smartsub.com";
        SignUpRequest otherSignUpRequest = new SignUpRequest(otherEmail, "password1234", "테스터B");
        mockMvc.perform(post("/api/v1/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(otherSignUpRequest)));

        SignInRequest otherSignInRequest = new SignInRequest(otherEmail, "password1234");

        String otherFirstLoginBody = mockMvc.perform(post("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherSignInRequest)))
            .andReturn().getResponse().getContentAsString();
        String otherFirstToken = objectMapper.readTree(otherFirstLoginBody).get("accessToken").asText();

        UUID otherUserId = extractUserIdFromToken(otherFirstToken);
        StoreCreateRequest otherStoreCreateRequest = new StoreCreateRequest(
            otherUserId, "테스트 매장B", "영업시간 9시부터", "당신은 매장의 AI 점장입니다.", StoreStatus.ACTIVE
        );
        String otherStoreResponseBody = mockMvc.perform(post("/api/v1/stores")
                .header("Authorization", "Bearer " + otherFirstToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherStoreCreateRequest)))
            .andReturn().getResponse().getContentAsString();

        otherStoreId = UUID.fromString(objectMapper.readTree(otherStoreResponseBody).get("storeId").asText());

        String otherSecondLoginBody = mockMvc.perform(post("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherSignInRequest)))
            .andReturn().getResponse().getContentAsString();
        otherAccessToken = objectMapper.readTree(otherSecondLoginBody).get("accessToken").asText();
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

    private WeeklyReport saveReport(UUID storeId, Map<ChatCategory, Long> categoryCounts) {
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        LocalDateTime weekEnd = LocalDateTime.now();
        WeeklyReport report = WeeklyReport.create(storeId, weekStart, weekEnd, categoryCounts);
        return weeklyReportRepository.save(report);
    }

    @Test
    @DisplayName("자기 매장의 리포트 목록만 조회된다")
    void getReports_returnsOnlyOwnStoreReports() throws Exception {
        // Given
        saveReport(storeId, Map.of(ChatCategory.MENU, 3L, ChatCategory.PARKING, 2L));
        saveReport(otherStoreId, Map.of(ChatCategory.EVENT, 4L));

        // When & Then
        mockMvc.perform(get("/api/v1/reports")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].topCategory").value("MENU"));
    }

    @Test
    @DisplayName("자기 매장 리포트 단건 조회 성공 시 200 OK를 반환한다")
    void getReport_ownStore_success() throws Exception {
        // Given
        WeeklyReport report = saveReport(storeId, Map.of(ChatCategory.MENU, 3L, ChatCategory.PARKING, 2L));

        // When & Then
        mockMvc.perform(get("/api/v1/reports/" + report.getId())
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCount").value(5))
            .andExpect(jsonPath("$.topCategory").value("MENU"))
            .andExpect(jsonPath("$.categoryDistribution.MENU").value(3))
            .andExpect(jsonPath("$.categoryDistribution.PARKING").value(2));
    }

    @Test
    @DisplayName("타 매장 리포트 접근 시 404 Not Found를 반환한다")
    void getReport_otherStore_returnsNotFound() throws Exception {
        // Given
        WeeklyReport otherReport = saveReport(otherStoreId, Map.of(ChatCategory.EVENT, 4L));

        // When & Then
        mockMvc.perform(get("/api/v1/reports/" + otherReport.getId())
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("REPORT_NOT_FOUND"));
    }

    @Test
    @DisplayName("존재하지 않는 리포트 조회 시 404 Not Found를 반환한다")
    void getReport_notFound_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/reports/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("REPORT_NOT_FOUND"));
    }
}