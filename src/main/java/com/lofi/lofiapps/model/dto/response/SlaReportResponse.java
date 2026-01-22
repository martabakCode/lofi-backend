package com.lofi.lofiapps.model.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlaReportResponse {
  private UUID loanId;
  private String customerName;
  private List<StageSlaInfo> stages;
  private Long totalDurationMinutes;

  @Data
  @Builder
  public static class StageSlaInfo {
    private String stage;
    private String status;
    private String actionBy;
    private Long durationMinutes;
  }
}
