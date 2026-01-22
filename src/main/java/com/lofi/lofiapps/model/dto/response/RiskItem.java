package com.lofi.lofiapps.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskItem {
  private String id;
  private String name;
  private String description;
  private RiskStatus status;
  private String comments;

  public enum RiskStatus {
    PASS,
    FAIL,
    WARNING,
    RESOLVED
  }
}
