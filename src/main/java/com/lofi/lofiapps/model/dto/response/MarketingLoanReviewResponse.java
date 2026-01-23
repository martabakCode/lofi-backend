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
public class MarketingLoanReviewResponse {
  private double confidence;
  private List<String> dataInconsistencies;
  private List<String> suggestedQuestions;
  private String notes;
}
