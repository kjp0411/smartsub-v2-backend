package com.smartsub.user.presentation;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsub.user.presentation.request.SignInRequest;
import com.smartsub.user.presentation.request.SignUpRequest;
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
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공 시 201 Created를 반환한다")
    @Transactional
    void signUp_success() throws Exception {
        // Given
        SignUpRequest request = new SignUpRequest("test@smartsub.com", "password1234", "테스터");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("이미 가입된 이메일로 회원가입 시 409 Conflict를 반환한다")
    @Transactional
    void signUp_duplicateEmail_returnsConflict() throws Exception {
        // Given
        SignUpRequest request = new SignUpRequest("duplicate@smartsub.com", "password1234", "테스터");
        mockMvc.perform(post("/api/v1/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));
    }

    @Test
    @DisplayName("필수 필드가 누락된 회원가입 요청은 400 Bad Request를 반환한다")
    @Transactional
    void signUp_invalidRequest_returnsBadRequest() throws Exception {
        // Given: 비밀번호가 8자 미만
        String invalidJson = """
            {
                "email": "test@smartsub.com",
                "password": "1234",
                "name": "테스터"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 성공 시 200 OK와 accessToken을 반환한다")
    @Transactional
    void signIn_success() throws Exception {
        // Given
        SignUpRequest signUpRequest = new SignUpRequest("login@smartsub.com", "password1234", "테스터");
        mockMvc.perform(post("/api/v1/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signUpRequest)));

        SignInRequest signInRequest = new SignInRequest("login@smartsub.com", "password1234");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken", notNullValue()));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 404 Not Found를 반환한다")
    @Transactional
    void signIn_userNotFound_returnsNotFound() throws Exception {
        // Given
        SignInRequest request = new SignInRequest("notexist@smartsub.com", "password1234");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 401 Unauthorized를 반환한다")
    @Transactional
    void signIn_invalidPassword_returnsUnauthorized() throws Exception {
        // Given
        SignUpRequest signUpRequest = new SignUpRequest("wrongpw@smartsub.com", "password1234", "테스터");
        mockMvc.perform(post("/api/v1/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signUpRequest)));

        SignInRequest signInRequest = new SignInRequest("wrongpw@smartsub.com", "wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("INVALID_PASSWORD"));
    }

    @Test
    @DisplayName("로그아웃 성공 시 200 OK를 반환한다")
    @Transactional
    void signOut_success() throws Exception {
        // Given
        SignUpRequest signUpRequest = new SignUpRequest("logout@smartsub.com", "password1234", "테스터");
        mockMvc.perform(post("/api/v1/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signUpRequest)));

        SignInRequest signInRequest = new SignInRequest("logout@smartsub.com", "password1234");
        String responseBody = mockMvc.perform(post("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)))
            .andReturn().getResponse().getContentAsString();

        String accessToken = objectMapper.readTree(responseBody).get("accessToken").asText();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signout")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk());
    }
}