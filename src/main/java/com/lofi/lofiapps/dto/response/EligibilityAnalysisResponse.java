package com.lofi.lofiapps.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityAnalysisResponse {
  private double confidence;
  private List<String> missingData;
  private List<String> potentialIssues;
  private List<String> notes;
}
