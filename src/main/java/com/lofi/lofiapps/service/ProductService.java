package com.lofi.lofiapps.service;

import com.lofi.lofiapps.dto.request.CreateProductRequest;
import com.lofi.lofiapps.dto.response.AvailableProductResponse;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.dto.response.ProductRecommendationResponse;
import com.lofi.lofiapps.dto.response.ProductResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface ProductService {
  ProductResponse createProduct(CreateProductRequest request);

  PagedResponse<ProductResponse> getProducts(Boolean isActive, Pageable pageable);

  ProductResponse getProductById(UUID id);

  ProductResponse updateProduct(
      UUID id, com.lofi.lofiapps.dto.request.UpdateProductRequest request);

  void deleteProduct(UUID id);

  ProductRecommendationResponse recommendProduct(UUID userId);

  ProductResponse getAssignedProduct(UUID userId);

  AvailableProductResponse getAvailableProduct(UUID userId);

  List<AvailableProductResponse> getAllAvailableProducts(UUID userId);
}
