package com.smartsub.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    @DisplayName("User.create()로 생성하면 입력한 필드가 정확히 설정된다")
    void create_setsFieldsCorrectly() {
        // Given
        String email = "test@smartsub.com";
        String passwordHash = "encodedPassword";
        String name = "테스터";
        UserRole role = UserRole.USER;

        // When
        User user = User.create(email, passwordHash, name, role);

        // Then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getRole()).isEqualTo(role);
    }

    @Test
    @DisplayName("User.create()로 생성하면 status는 기본값 ACTIVE로 설정된다")
    void create_setsDefaultStatusToActive() {
        // When
        User user = User.create("test@smartsub.com", "encodedPassword", "테스터", UserRole.USER);

        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
}