package com.smartsub.global.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SystemAuthRunner {

    private final SystemAuthContext systemAuthContext;

    public void runAsSystem(Runnable task) {
        Authentication systemAuth = new UsernamePasswordAuthenticationToken(
            systemAuthContext.getSystemUserId(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_SYSTEM"))
        );

        SecurityContextHolder.getContext().setAuthentication(systemAuth);
        try {
            task.run();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}