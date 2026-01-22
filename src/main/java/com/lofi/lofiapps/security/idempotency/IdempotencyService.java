package com.lofi.lofiapps.security.idempotency;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdempotencyService {
  private final RedisTemplate<String, Object> redisTemplate;
  private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";

  public boolean isDuplicate(String key) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(IDEMPOTENCY_KEY_PREFIX + key));
  }

  public void saveResponse(String key, Object response, long timeoutInHours) {
    redisTemplate
        .opsForValue()
        .set(IDEMPOTENCY_KEY_PREFIX + key, response, timeoutInHours, TimeUnit.HOURS);
  }

  public Object getResponse(String key) {
    return redisTemplate.opsForValue().get(IDEMPOTENCY_KEY_PREFIX + key);
  }
}
