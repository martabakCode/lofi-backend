package com.lofi.lofiapps.model.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecommendationResponse {
  private double confidence;
  private String recommendedProductCode;
  private String recommendedProductName;
  private String reasoning;
  private List<String> limitations;
}
