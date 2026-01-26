package com.lofi.lofiapps.service.impl.usecase.product;

import com.lofi.lofiapps.dto.response.ProductRecommendationResponse;
import com.lofi.lofiapps.entity.Product;
import com.lofi.lofiapps.entity.User;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendProductUseCase {

  public ProductRecommendationResponse execute(User user, List<Product> products) {
    log.info("Generating product recommendation for user: {}", user.getId());

    Product recommended = products.isEmpty() ? null : products.get(0);

    return ProductRecommendationResponse.builder()
        .confidence(0.8)
        .recommendedProductCode(recommended != null ? recommended.getProductCode() : "N/A")
        .recommendedProductName(recommended != null ? recommended.getProductName() : "N/A")
        .reasoning("Recommended based on initial user profile and active products.")
        .limitations(Collections.emptyList())
        .build();
  }
}
