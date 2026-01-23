package com.lofi.lofiapps.service;

import com.lofi.lofiapps.dto.response.LoanKpiResponse;
import com.lofi.lofiapps.dto.response.SlaReportResponse;
import java.util.UUID;

public interface ReportService {
  LoanKpiResponse getLoanKpis();

  byte[] exportLoanKpis();

  SlaReportResponse getSlaReport(UUID loanId);

  byte[] exportSlaReport(UUID loanId);
}
