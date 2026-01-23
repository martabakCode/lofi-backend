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
public class BranchManagerSupportResponse {
  private double confidence;
  private List<String> branchRisks;
  private List<String> attentionPoints;
  private List<String> limitations;
}
