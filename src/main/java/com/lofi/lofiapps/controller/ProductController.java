package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.model.dto.request.CreateProductRequest;
import com.lofi.lofiapps.model.dto.response.*;
import com.lofi.lofiapps.model.dto.response.PagedResponse;
import com.lofi.lofiapps.model.dto.response.ProductResponse;
import com.lofi.lofiapps.service.impl.product.CreateProductUseCase;
import com.lofi.lofiapps.service.impl.product.GetProductsUseCase;
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
  private final CreateProductUseCase createProductUseCase;
  private final GetProductsUseCase getProductsUseCase;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
      @Valid @RequestBody CreateProductRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(createProductUseCase.execute(request), "Product created successfully"));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getProducts(
      @RequestParam(required = false) Boolean isActive,
      @PageableDefault(size = 10) Pageable pageable) {
    return ResponseEntity.ok(ApiResponse.success(getProductsUseCase.execute(isActive, pageable)));
  }
}
