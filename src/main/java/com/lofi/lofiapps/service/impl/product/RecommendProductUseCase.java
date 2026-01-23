package com.lofi.lofiapps.service.impl.product;

import com.lofi.lofiapps.model.dto.response.ProductRecommendationResponse;
import com.lofi.lofiapps.model.entity.Product;
import com.lofi.lofiapps.model.entity.User;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Recommend Product UseCase.
 *
 * <p>Per MCP Rules & Workflow Section 5 (Product Assignment Logic):
 *
 * <p>Section 5.1 - Default Product Assignment: - Sistem otomatis assign product TERENDAH -
 * Berdasarkan: Penghasilan, DBR, Risiko awal - Customer TIDAK bisa menaikkan product
 *
 * <p>Section 5.2 - Product Upgrade (ADMIN ONLY): - Marketing / BM / BO bisa menaikkan product -
 * Dengan justifikasi - Tercatat audit
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendProductUseCase {

  /**
   * Recommend suitable product based on user profile.
   *
   * <p>Per Workflow Section 5.1: - Automatically assigns the LOWEST eligible product - Based on
   * income, DBR, and initial risk
   *
   * @param user the user to recommend product for
   * @param products list of available products
   * @return product recommendation response with reasoning
   */
  public ProductRecommendationResponse execute(User user, List<Product> products) {
    log.info("Executing RecommendProductUseCase for user: {}", user.getId());

    double confidence = 0.9;
    List<String> limitations = new ArrayList<>();
    List<String> reasoningList = new ArrayList<>();
    Product recommended = null;

    // Per Workflow Section 4.3 - Income check required
    if (user.getUserBiodata() == null || user.getUserBiodata().getMonthlyIncome() == null) {
      return ProductRecommendationResponse.builder()
          .confidence(0.0)
          .reasoning("Cannot recommend product: Income data missing")
          .limitations(List.of("Missing Income"))
          .build();
    }

    BigDecimal income = user.getUserBiodata().getMonthlyIncome();

    // Per Workflow Section 9.2 - DBR maksimal: 30–35%
    BigDecimal maxInstallment = income.multiply(new BigDecimal("0.35"), MathContext.DECIMAL32);

    // Per Section 5.1 - Sort by lowest product first (assign TERENDAH)
    List<Product> eligibleProducts =
        products.stream()
            .filter(p -> p.getIsActive() != null && p.getIsActive())
            .sorted(Comparator.comparing(Product::getMaxLoanAmount))
            .collect(Collectors.toList());

    for (Product product : eligibleProducts) {
      BigDecimal minAmt = product.getMinLoanAmount();
      int maxTenor = product.getMaxTenor();
      if (maxTenor <= 0) {
        continue;
      }

      BigDecimal approxMinInstallment =
          minAmt.divide(BigDecimal.valueOf(maxTenor), MathContext.DECIMAL32);

      if (approxMinInstallment.compareTo(maxInstallment) <= 0) {
        recommended = product;
        reasoningList.add(
            "Selected "
                + product.getProductName()
                + " as the entry level product fitting DBR capacity.");
        break;
      }
    }

    if (recommended == null) {
      return ProductRecommendationResponse.builder()
          .confidence(0.0)
          .reasoning("No eligible product found for this income level.")
          .limitations(List.of("Income too low for any product", "DBR Threshold Exceeded"))
          .build();
    }

    // Low income segment warning
    if (income.compareTo(new BigDecimal("3000000")) < 0) {
      limitations.add("Low income segment (< 3jt)");
    }

    return ProductRecommendationResponse.builder()
        .confidence(confidence)
        .recommendedProductCode(recommended.getProductCode())
        .recommendedProductName(recommended.getProductName())
        .reasoning(String.join("; ", reasoningList))
        .limitations(limitations)
        .build();
  }
}
