package com.smartsub.global.config;

import com.smartsub.user.domain.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SystemAuthContext {

    private final UserRepository userRepository;
    private volatile UUID systemUserId;

    public UUID getSystemUserId() {
        if (systemUserId == null) {
            systemUserId = userRepository.findByEmail(SystemUserConstants.SYSTEM_EMAIL)
                .orElseThrow(() -> new IllegalStateException("SYSTEM 사용자가 초기화되지 않았습니다."))
                .getId();
        }
        return systemUserId;
    }
}