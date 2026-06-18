package com.smartsub.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartsub.global.exception.BusinessException;
import com.smartsub.global.jwt.JwtTokenProvider;
import com.smartsub.global.jwt.TokenBlacklistRepository;
import com.smartsub.store.domain.StoreRepository;
import com.smartsub.user.application.dto.AuthResult;
import com.smartsub.user.application.dto.SignInCommand;
import com.smartsub.user.application.dto.SignUpCommand;
import com.smartsub.user.domain.User;
import com.smartsub.user.domain.UserRepository;
import com.smartsub.user.domain.UserRole;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 성공 시 User가 저장된다")
    void signUp_success() {
        // Given
        SignUpCommand command = new SignUpCommand("test@smartsub.com", "password123", "테스터");
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(passwordEncoder.encode(command.password())).thenReturn("encodedPassword");

        // When
        authService.signUp(command);

        // Then
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입하면 예외가 발생한다")
    void signUp_duplicateEmail_throwsException() {
        // Given
        SignUpCommand command = new SignUpCommand("test@smartsub.com", "password123", "테스터");
        when(userRepository.existsByEmail(command.email())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.signUp(command)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("로그인 성공 시 JWT 토큰을 반환한다")
    void signIn_success() {
        // Given
        SignInCommand command = new SignInCommand("test@smartsub.com", "password123");
        User user = User.create("test@smartsub.com", "encodedPassword", "테스터", UserRole.USER);

        when(userRepository.findByEmail(command.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(command.password(), user.getPasswordHash())).thenReturn(true);
        when(storeRepository.findByUserIdAndDeletedAtIsNull(any())).thenReturn(Optional.empty());
        when(jwtTokenProvider.generateAccessToken(any(), any(), any(), any())).thenReturn("mock-token");

        // When
        AuthResult result = authService.signIn(command);

        // Then
        assertThat(result.accessToken()).isEqualTo("mock-token");
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다")
    void signIn_invalidPassword_throwsException() {
        // Given
        SignInCommand command = new SignInCommand("test@smartsub.com", "wrongPassword");
        User user = User.create("test@smartsub.com", "encodedPassword", "테스터", UserRole.USER);

        when(userRepository.findByEmail(command.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(command.password(), user.getPasswordHash())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.signIn(command)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("로그아웃 시 토큰이 블랙리스트에 등록된다")
    void signOut_success() {
        // Given
        String token = "valid-token";
        when(jwtTokenProvider.getRemainingExpiration(token)).thenReturn(60000L);

        // When
        authService.signOut(token);

        // Then
        verify(tokenBlacklistRepository).addToBlacklist(token, 60000L);
    }
}