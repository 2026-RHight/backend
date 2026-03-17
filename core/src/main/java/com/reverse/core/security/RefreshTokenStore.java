package com.reverse.core.security;

import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String PREFIX = "rt:emp:";
    private static final DefaultRedisScript<Long> ROTATE_IF_MATCHES_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local current = redis.call('GET', KEYS[1])
                    if current == false then
                        return 0
                    end
                    if current ~= ARGV[1] then
                        return 0
                    end
                    redis.call('PSETEX', KEYS[1], ARGV[3], ARGV[2])
                    return 1
                    """,
                    Long.class);

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

    public boolean rotateIfMatches(
            Long employeeId, String oldRefreshToken, String newRefreshToken) {
        if (employeeId == null
                || oldRefreshToken == null
                || oldRefreshToken.isBlank()
                || newRefreshToken == null
                || newRefreshToken.isBlank()) {
            return false;
        }
        Long result =
                redisTemplate.execute(
                        ROTATE_IF_MATCHES_SCRIPT,
                        List.of(key(employeeId)),
                        oldRefreshToken,
                        newRefreshToken,
                        String.valueOf(refreshExpirationMillis));
        return Long.valueOf(1L).equals(result);
    }

    public void deleteByEmployeeId(Long employeeId) {
        if (employeeId == null) return;
        redisTemplate.delete(key(employeeId));
    }

    private String key(Long employeeId) {
        return PREFIX + employeeId;
    }
}
