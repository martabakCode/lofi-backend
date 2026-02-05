package com.lofi.lofiapps.service;

import org.springframework.stereotype.Service;

@Service
public interface RateLimitService {

  /**
   * Try to consume a token from the rate limit bucket.
   *
   * @param key The rate limit key (e.g., userId or IP)
   * @param maxRequests Maximum requests allowed per window
   * @param windowMinutes Window size in minutes
   * @return true if token was consumed, false if rate limited
   */
  boolean tryConsume(String key, int maxRequests, int windowMinutes);

  /**
   * Get remaining requests for a key.
   *
   * @param key The rate limit key
   * @param maxRequests Maximum requests allowed per window
   * @param windowMinutes Window size in minutes
   * @return Remaining requests count
   */
  int getRemainingRequests(String key, int maxRequests, int windowMinutes);

  /**
   * Reset rate limit for a key.
   *
   * @param key The rate limit key
   */
  void reset(String key);
}
