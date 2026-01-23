package com.lofi.lofiapps.model.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BackOfficeRiskEvaluationResponse {
  private Double confidence;
  private String riskOverview;
  private List<String> keyRiskFactors;
  private List<String> verificationChecklist;
  private List<String> limitations;
}
