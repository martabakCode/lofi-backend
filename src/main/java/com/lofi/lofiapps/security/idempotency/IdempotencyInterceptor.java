package com.lofi.lofiapps.security.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {
  private final IdempotencyService idempotencyService;
  private final ObjectMapper objectMapper;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String idempotencyKey = request.getHeader("Idempotency-Key");
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      return true;
    }

    if (idempotencyService.isDuplicate(idempotencyKey)) {
      Object cachedResponse = idempotencyService.getResponse(idempotencyKey);
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.print(objectMapper.writeValueAsString(cachedResponse));
      out.flush();
      return false;
    }

    return true;
  }
}
