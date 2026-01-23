package com.lofi.lofiapps.dto.response;

import com.lofi.lofiapps.enums.ApprovalStage;
import com.lofi.lofiapps.enums.LoanStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanResponse {
  private UUID id;
  private UUID customerId;
  private String customerName;
  private ProductResponse product;
  private BigDecimal loanAmount;
  private Integer tenor;
  private LoanStatus loanStatus;
  private ApprovalStage currentStage;
  private LocalDateTime submittedAt;
  private LocalDateTime approvedAt;
  private LocalDateTime rejectedAt;
  private LocalDateTime disbursedAt;
  private java.util.List<com.lofi.lofiapps.dto.response.DocumentResponse> documents;
  private String disbursementReference;
  private LoanAnalysisResponse aiAnalysis;
}
