package com.lofi.lofiapps.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lofi.lofiapps.dto.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Simple rate limiting filter for authentication endpoints. Uses in-memory token bucket algorithm
 * per IP address.
 */
@Slf4j
@Component
@Order(1) // Execute before other filters
public class RateLimitFilter extends OncePerRequestFilter {

  // Token bucket: IP -> [tokens, lastRefillTime]
  private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

  // Rate limit configuration
  private static final int MAX_REQUESTS = 5; // 5 requests
  private static final long REFILL_PERIOD_MS = TimeUnit.MINUTES.toMillis(1); // per minute

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();

    // Only apply rate limiting to auth endpoints
    if (!isAuthEndpoint(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    String clientIp = getClientIp(request);
    Bucket bucket = buckets.computeIfAbsent(clientIp, k -> new Bucket());

    if (!bucket.tryConsume()) {
      log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
      sendRateLimitResponse(response);
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean isAuthEndpoint(String path) {
    return path.startsWith("/auth/login")
        || path.startsWith("/auth/register")
        || path.startsWith("/auth/google")
        || path.startsWith("/auth/forgot-password")
        || path.startsWith("/auth/reset-password")
        || path.startsWith("/users/me/pin")
        || path.startsWith("/users/set-pin");
  }

  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ApiResponse<Object> apiResponse =
        ApiResponse.error(
            "RATE_LIMIT_EXCEEDED", "Too many requests. Please try again later.", null);

    ObjectMapper mapper = new ObjectMapper();
    response.getWriter().write(mapper.writeValueAsString(apiResponse));
  }

  /** Simple token bucket implementation. */
  private static class Bucket {
    private int tokens = MAX_REQUESTS;
    private long lastRefillTime = System.currentTimeMillis();

    synchronized boolean tryConsume() {
      refill();
      if (tokens > 0) {
        tokens--;
        return true;
      }
      return false;
    }

    private void refill() {
      long now = System.currentTimeMillis();
      long timePassed = now - lastRefillTime;
      if (timePassed >= REFILL_PERIOD_MS) {
        tokens = MAX_REQUESTS;
        lastRefillTime = now;
      }
    }
  }
}
