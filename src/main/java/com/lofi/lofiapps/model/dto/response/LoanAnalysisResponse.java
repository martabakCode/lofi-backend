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
public class LoanAnalysisResponse {
  private double confidence;
  private String summary;
  private List<String> riskFlags;
  private List<String> reviewNotes;
  private List<String> limitations;
}
