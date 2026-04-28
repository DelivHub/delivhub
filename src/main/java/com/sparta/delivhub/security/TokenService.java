package com.sparta.delivhub.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisTemplate<String, String> redisTemplate;

    // refreshToken
    private static final String RT_PREFIX = "RT:";

    public void saveRefreshToken(String username, String refreshToken, long duration) {
        redisTemplate.opsForValue()
                .set(RT_PREFIX + username, refreshToken, duration, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get(RT_PREFIX + username);
    }

    public void deleteRefreshToken(String username) {
        redisTemplate.delete(RT_PREFIX + username);
    }
}
