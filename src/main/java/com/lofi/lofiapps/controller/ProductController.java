package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.model.dto.request.CreateProductRequest;
import com.lofi.lofiapps.model.dto.response.*;
import com.lofi.lofiapps.model.dto.response.PagedResponse;
import com.lofi.lofiapps.model.dto.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
  private final com.lofi.lofiapps.service.ProductService productService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
      @Valid @RequestBody CreateProductRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(productService.createProduct(request), "Product created successfully"));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getProducts(
      @RequestParam(required = false) Boolean isActive,
      @PageableDefault(size = 10) Pageable pageable) {
    return ResponseEntity.ok(ApiResponse.success(productService.getProducts(isActive, pageable)));
  }

  @GetMapping("/recommendation")
  @PreAuthorize("hasRole('ADMIN') or hasRole('MARKETING') or hasRole('CUSTOMER')")
  @io.swagger.v3.oas.annotations.Operation(summary = "Get AI Product Recommendation for a User")
  public ResponseEntity<ApiResponse<ProductRecommendationResponse>> recommendProduct(
      @RequestParam(required = false) java.util.UUID userId,
      @org.springframework.security.core.annotation.AuthenticationPrincipal
          com.lofi.lofiapps.security.service.UserPrincipal userPrincipal) {

    java.util.UUID targetUserId = userId;
    if (targetUserId == null) {
      if (userPrincipal == null) {
        throw new IllegalArgumentException("User ID is required when not logged in");
      }
      targetUserId = userPrincipal.getId();
    }
    // TODO: Add permission check if userId != principal.id

    return ResponseEntity.ok(ApiResponse.success(productService.recommendProduct(targetUserId)));
  }
}
