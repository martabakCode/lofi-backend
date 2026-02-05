package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.dto.request.CreateProductRequest;
import com.lofi.lofiapps.dto.response.*;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.dto.response.ProductResponse;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product Management")
public class ProductController {
  private final ProductService productService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Create a new product")
  public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
      @Valid @RequestBody CreateProductRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(productService.createProduct(request), "Product created successfully"));
  }

  @GetMapping
  @Operation(summary = "Get all products")
  public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getProducts(
      @RequestParam(required = false) Boolean isActive,
      @PageableDefault(size = 10) Pageable pageable) {
    return ResponseEntity.ok(ApiResponse.success(productService.getProducts(isActive, pageable)));
  }

  @GetMapping("/recommendation")
  @PreAuthorize("hasRole('ADMIN') or hasRole('MARKETING') or hasRole('CUSTOMER')")
  @Operation(summary = "Get AI Product Recommendation for a User")
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

  @GetMapping("/me")
  @PreAuthorize("hasRole('CUSTOMER')")
  @Operation(summary = "Get the product assigned to the logged-in customer")
  public ResponseEntity<ApiResponse<ProductResponse>> getAssignedProduct(
      @org.springframework.security.core.annotation.AuthenticationPrincipal
          com.lofi.lofiapps.security.service.UserPrincipal userPrincipal) {

    if (userPrincipal == null) {
      throw new IllegalArgumentException("User is not authenticated");
    }

    return ResponseEntity.ok(
        ApiResponse.success(productService.getAssignedProduct(userPrincipal.getId())));
  }

  @GetMapping("/available")
  @PreAuthorize("hasRole('CUSTOMER')")
  @Operation(summary = "Get available product limit with loan deductions")
  public ResponseEntity<ApiResponse<AvailableProductResponse>> getAvailableProduct(
      @org.springframework.security.core.annotation.AuthenticationPrincipal
          UserPrincipal userPrincipal) {

    return ResponseEntity.ok(
        ApiResponse.success(productService.getAvailableProduct(userPrincipal.getId())));
  }

  @GetMapping("/available/all")
  @PreAuthorize("hasRole('CUSTOMER')")
  @Operation(summary = "Get all available products with loan deductions")
  public ResponseEntity<ApiResponse<List<AvailableProductResponse>>> getAllAvailableProducts(
      @org.springframework.security.core.annotation.AuthenticationPrincipal
          UserPrincipal userPrincipal) {

    return ResponseEntity.ok(
        ApiResponse.success(productService.getAllAvailableProducts(userPrincipal.getId())));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Get product by ID")
  public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Update a product")
  public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
      @PathVariable UUID id,
      @Valid @RequestBody com.lofi.lofiapps.dto.request.UpdateProductRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            productService.updateProduct(id, request), "Product updated successfully"));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Delete a product")
  public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
    productService.deleteProduct(id);
    return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
  }
}
