package com.smartsub.user.application;

import com.smartsub.global.exception.BusinessException;
import com.smartsub.global.exception.ErrorCode;
import com.smartsub.global.jwt.JwtTokenProvider;
import com.smartsub.store.domain.StoreRepository;
import com.smartsub.user.application.dto.AuthResult;
import com.smartsub.user.application.dto.SignInCommand;
import com.smartsub.user.application.dto.SignUpCommand;
import com.smartsub.user.domain.User;
import com.smartsub.user.domain.UserRepository;
import com.smartsub.user.domain.UserRole;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signUp(SignUpCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String passwordHash = passwordEncoder.encode(command.password());

        User user = User.create(
            command.email(),
            passwordHash,
            command.name(),
            UserRole.USER
        );

        userRepository.save(user);
    }

    public AuthResult signIn(SignInCommand command) {
        User user = userRepository.findByEmail(command.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        UUID storeId = storeRepository.findByUserIdAndDeletedAtIsNull(user.getId())
            .map(store -> store.getId())
            .orElse(null);

        String token = jwtTokenProvider.generateAccessToken(
            user.getId(),
            user.getEmail(),
            user.getRole().name(),
            storeId
        );

        return new AuthResult(token);
    }
}
