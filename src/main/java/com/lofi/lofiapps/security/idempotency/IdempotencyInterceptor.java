package com.lofi.lofiapps.security.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for handling idempotency of state-changing operations. Per workflow rules (OJK/BI
 * compliant): - Approve, Disburse, Reset Password MUST be idempotent - Duplicate requests with same
 * Idempotency-Key return cached response
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {
  private final IdempotencyService idempotencyService;
  private final ObjectMapper objectMapper;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    // Only handle method handlers
    if (!(handler instanceof HandlerMethod)) {
      return true;
    }

    HandlerMethod handlerMethod = (HandlerMethod) handler;
    RequireIdempotency annotation = handlerMethod.getMethodAnnotation(RequireIdempotency.class);

    String idempotencyKey = request.getHeader("Idempotency-Key");

    // If method requires idempotency, key is MANDATORY
    if (annotation != null) {
      if (idempotencyKey == null || idempotencyKey.isBlank()) {
        log.warn(
            "[IDEMPOTENCY] Missing Idempotency-Key for method: {}.{}",
            handlerMethod.getBeanType().getSimpleName(),
            handlerMethod.getMethod().getName());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(
            "{\"success\":false,\"message\":\"Idempotency-Key header is required for this operation\",\"code\":\"IDEMPOTENCY_KEY_REQUIRED\"}");
        out.flush();
        return false;
      }
    }

    // If no key provided and not required, proceed normally
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      return true;
    }

    // Check for duplicate request
    if (idempotencyService.isDuplicate(idempotencyKey)) {
      log.info("[IDEMPOTENCY] Duplicate request detected for key: {}", idempotencyKey);
      Object cachedResponse = idempotencyService.getResponse(idempotencyKey);
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.print(objectMapper.writeValueAsString(cachedResponse));
      out.flush();
      return false;
    }

    // Store key in request for response caching
    request.setAttribute("idempotencyKey", idempotencyKey);
    request.setAttribute("idempotencyTtl", annotation != null ? annotation.ttlHours() : 24L);

    return true;
  }
}
