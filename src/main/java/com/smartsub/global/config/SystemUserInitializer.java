package com.smartsub.global.config;

import com.smartsub.user.domain.User;
import com.smartsub.user.domain.UserRepository;
import com.smartsub.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import static com.smartsub.global.config.SystemUserConstants.SYSTEM_EMAIL;

@Component
@RequiredArgsConstructor
public class SystemUserInitializer implements CommandLineRunner {


    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(SYSTEM_EMAIL)) {
            User systemUser = User.create(
                SYSTEM_EMAIL,
                "SYSTEM_NO_LOGIN",
                "System",
                UserRole.SYSTEM
            );
            userRepository.save(systemUser);
        }
    }
}
