package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.service.RateLimitService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitServiceImpl implements RateLimitService {

  private final RedisTemplate<String, Object> redisTemplate;

  private static final String RATE_LIMIT_KEY_PREFIX = "ratelimit:";

  @Override
  public boolean tryConsume(String key, int maxRequests, int windowMinutes) {
    String redisKey = RATE_LIMIT_KEY_PREFIX + key;

    // Use increment which returns the new value
    Long newCount = redisTemplate.opsForValue().increment(redisKey);

    // If it's the first request (newCount == 1), set expiration
    if (newCount != null && newCount == 1) {
      redisTemplate.expire(redisKey, windowMinutes, TimeUnit.MINUTES);
    }

    return newCount != null && newCount <= maxRequests;
  }

  @Override
  public int getRemainingRequests(String key, int maxRequests, int windowMinutes) {
    String redisKey = RATE_LIMIT_KEY_PREFIX + key;
    Object value = redisTemplate.opsForValue().get(redisKey);

    int current = 0;
    if (value instanceof Integer) {
      current = (Integer) value;
    } else if (value instanceof Long) {
      current = ((Long) value).intValue();
    }

    return Math.max(0, maxRequests - current);
  }

  @Override
  public void reset(String key) {
    String redisKey = RATE_LIMIT_KEY_PREFIX + key;
    redisTemplate.delete(redisKey);
  }
}
