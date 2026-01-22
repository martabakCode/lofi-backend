package com.lofi.lofiapps.security.idempotency;

import com.lofi.lofiapps.model.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class IdempotencyResponseAdvice implements ResponseBodyAdvice<Object> {
  private final IdempotencyService idempotencyService;

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {

    HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
    String idempotencyKey = servletRequest.getHeader("Idempotency-Key");

    if (idempotencyKey != null && !idempotencyKey.isBlank() && body instanceof ApiResponse) {
      ApiResponse<?> apiResponse = (ApiResponse<?>) body;
      // Only cache successful or specific results
      idempotencyService.saveResponse(idempotencyKey, apiResponse, 24);
    }

    return body;
  }
}
