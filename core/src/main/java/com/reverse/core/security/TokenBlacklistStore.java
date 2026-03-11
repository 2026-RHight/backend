package com.reverse.core.security;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlacklistStore {

    private static final String PREFIX = "bl:";

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    // 토큰을 블랙리스트에 등록하는 메서드
    public void blacklist(String token) {
        long ttlMillis = jwtTokenProvider.getRemainingMillis(token);
        if (ttlMillis <= 0) return; // 이미 만료된 토큰일 경우 저장하지 않고 리턴

        // redis에 key-value 저장
        redisTemplate
                .opsForValue()
                .set(
                        PREFIX + token, // key = "bl:" + 토큰 문자열
                        "logout",
                        ttlMillis, // TTL = 만료까지 남은 시간
                        TimeUnit.MILLISECONDS // TTL 단위 = ms
                        );
    }

    // 들어온 토큰이 블랙리스트인지 확인
    public boolean isBlacklisted(String token) {
        // null 값을 방지하기 위해
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
    }
}
