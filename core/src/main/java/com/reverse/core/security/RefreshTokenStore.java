package com.reverse.core.security;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String PREFIX = "rt:emp:";

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMillis;

    public void save(Long employeeId, String refreshToken) {
        if (employeeId == null || refreshToken == null || refreshToken.isBlank()) return;
        redisTemplate
                .opsForValue()
                .set(key(employeeId), refreshToken, refreshExpirationMillis, TimeUnit.MILLISECONDS);
    }

    public boolean matches(Long employeeId, String refreshToken) {
        if (employeeId == null || refreshToken == null || refreshToken.isBlank()) return false;
        String storedToken = redisTemplate.opsForValue().get(key(employeeId));
        return refreshToken.equals(storedToken);
    }

    public void deleteByEmployeeId(Long employeeId) {
        if (employeeId == null) return;
        redisTemplate.delete(key(employeeId));
    }

    private String key(Long employeeId) {
        return PREFIX + employeeId;
    }
}
