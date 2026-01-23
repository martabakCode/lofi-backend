package com.lofi.lofiapps.service;

import com.lofi.lofiapps.model.dto.request.CreateProductRequest;
import com.lofi.lofiapps.model.dto.response.PagedResponse;
import com.lofi.lofiapps.model.dto.response.ProductRecommendationResponse;
import com.lofi.lofiapps.model.dto.response.ProductResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface ProductService {
  ProductResponse createProduct(CreateProductRequest request);

  PagedResponse<ProductResponse> getProducts(Boolean isActive, Pageable pageable);

  // AI
  ProductRecommendationResponse recommendProduct(UUID userId);
}
