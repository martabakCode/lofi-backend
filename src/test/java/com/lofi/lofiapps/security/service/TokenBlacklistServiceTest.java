package com.lofi.lofiapps.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

  @Mock private StringRedisTemplate redisTemplate;

  @Mock private ValueOperations<String, String> valueOperations;

  @InjectMocks private TokenBlacklistService tokenBlacklistService;

  @BeforeEach
  void setUp() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
  }

  @Test
  void blacklistToken_ShouldSetValueInRedis() {
    String token = "testToken";
    long expirationMs = 3600000;

    tokenBlacklistService.blacklistToken(token, expirationMs);

    verify(valueOperations)
        .set("BLACKLIST_JWT:" + token, "true", expirationMs, TimeUnit.MILLISECONDS);
  }

  @Test
  void isBlacklisted_WhenRedisReturnsTrue_ShouldReturnTrue() {
    String token = "blacklistedToken";
    when(valueOperations.get("BLACKLIST_JWT:" + token)).thenReturn("true");

    boolean result = tokenBlacklistService.isBlacklisted(token);

    assertTrue(result);
  }

  @Test
  void isBlacklisted_WhenRedisReturnsNull_ShouldReturnFalse() {
    String token = "cleanToken";
    when(valueOperations.get("BLACKLIST_JWT:" + token)).thenReturn(null);

    boolean result = tokenBlacklistService.isBlacklisted(token);

    assertFalse(result);
  }

  @Test
  void forceLogoutUser_ShouldSetTimestampInRedis() {
    UUID userId = UUID.randomUUID();

    tokenBlacklistService.forceLogoutUser(userId);

    verify(valueOperations)
        .set(eq("FORCED_LOGOUT_USER:" + userId), anyString(), eq(24L), eq(TimeUnit.HOURS));
  }

  @Test
  void getForcedLogoutTimestamp_WhenExists_ShouldReturnTimestamp() {
    UUID userId = UUID.randomUUID();
    long timestamp = System.currentTimeMillis();
    when(valueOperations.get("FORCED_LOGOUT_USER:" + userId)).thenReturn(String.valueOf(timestamp));

    long result = tokenBlacklistService.getForcedLogoutTimestamp(userId);

    assertEquals(timestamp, result);
  }

  @Test
  void getForcedLogoutTimestamp_WhenNotExists_ShouldReturnZero() {
    UUID userId = UUID.randomUUID();
    when(valueOperations.get("FORCED_LOGOUT_USER:" + userId)).thenReturn(null);

    long result = tokenBlacklistService.getForcedLogoutTimestamp(userId);

    assertEquals(0L, result);
  }
}
