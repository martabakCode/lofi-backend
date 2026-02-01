package com.lofi.lofiapps.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for rate limiting. Protects authentication endpoints from brute force attacks. */
@Configuration
public class RateLimitConfig {

  /** Rate limiter for authentication endpoints. Limits to 5 requests per minute per IP/user. */
  @Bean
  public RateLimiter authRateLimiter() {
    RateLimiterConfig config =
        RateLimiterConfig.custom()
            .limitForPeriod(5)
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ofSeconds(0))
            .build();

    RateLimiterRegistry registry = RateLimiterRegistry.of(config);
    return registry.rateLimiter("auth");
  }

  /** Rate limiter for general API endpoints. Limits to 100 requests per minute per user. */
  @Bean
  public RateLimiter apiRateLimiter() {
    RateLimiterConfig config =
        RateLimiterConfig.custom()
            .limitForPeriod(100)
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ofSeconds(0))
            .build();

    RateLimiterRegistry registry = RateLimiterRegistry.of(config);
    return registry.rateLimiter("api");
  }
}
