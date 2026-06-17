package com.smartsub.global.jwt;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TokenBlacklistRepository {

    private static final String PREFIX = "blacklist:";

    private final StringRedisTemplate redisTemplate;

    public void addToBlacklist(String token, long expirationMillis) {
        String key = PREFIX + token;
        redisTemplate.opsForValue().set(key, "logout", Duration.ofMillis(expirationMillis));
    }

    public boolean isBlacklisted(String token) {
        String key = PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}