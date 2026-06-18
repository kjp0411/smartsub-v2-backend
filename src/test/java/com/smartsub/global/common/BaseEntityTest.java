package com.smartsub.global.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartsub.user.domain.User;
import com.smartsub.user.domain.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BaseEntityTest {

    @Test
    @DisplayName("생성 직후에는 삭제되지 않은 상태이다")
    void newEntity_isNotDeleted() {
        // Given
        User user = User.create("test@smartsub.com", "encodedPassword", "테스터", UserRole.USER);

        // When & Then
        assertThat(user.isDeleted()).isFalse();
        assertThat(user.getDeletedAt()).isNull();
        assertThat(user.getDeletedBy()).isNull();
    }

    @Test
    @DisplayName("delete()를 호출하면 deletedAt과 deletedBy가 설정되고 isDeleted()는 true가 된다")
    void delete_setsDeletedAtAndDeletedBy() {
        // Given
        User user = User.create("test@smartsub.com", "encodedPassword", "테스터", UserRole.USER);
        UUID deleterId = UUID.randomUUID();

        // When
        user.delete(deleterId);

        // Then
        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.getDeletedBy()).isEqualTo(deleterId);
    }
}