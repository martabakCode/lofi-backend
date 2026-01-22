package com.lofi.lofiapps.security.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
  private final StringRedisTemplate redisTemplate;

  public void blacklistToken(String token, long expirationMs) {
    String key = "BLACKLIST_JWT:" + token;
    redisTemplate.opsForValue().set(key, "true", expirationMs, TimeUnit.MILLISECONDS);
  }

  public boolean isBlacklisted(String token) {
    String key = "BLACKLIST_JWT:" + token;
    return Boolean.TRUE.toString().equals(redisTemplate.opsForValue().get(key));
  }

  public void forceLogoutUser(UUID userId) {
    String key = "FORCED_LOGOUT_USER:" + userId.toString();
    // Store current timestamp as the threshold. Tokens issued before this are
    // invalid.
    redisTemplate
        .opsForValue()
        .set(key, String.valueOf(System.currentTimeMillis()), 24, TimeUnit.HOURS);
  }

  public long getForcedLogoutTimestamp(UUID userId) {
    String key = "FORCED_LOGOUT_USER:" + userId.toString();
    String val = redisTemplate.opsForValue().get(key);
    return val != null ? Long.parseLong(val) : 0;
  }
}
