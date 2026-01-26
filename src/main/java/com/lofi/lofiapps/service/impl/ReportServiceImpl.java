package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.dto.response.LoanKpiResponse;
import com.lofi.lofiapps.dto.response.SlaReportResponse;
import com.lofi.lofiapps.service.ReportService;
import com.lofi.lofiapps.service.impl.usecase.report.ExcelExportService;
import com.lofi.lofiapps.service.impl.usecase.report.GetLoanKpisUseCase;
import com.lofi.lofiapps.service.impl.usecase.report.GetSlaReportUseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

  private final GetLoanKpisUseCase getLoanKpisUseCase;
  private final GetSlaReportUseCase getSlaReportUseCase;
  private final ExcelExportService excelExportService;

  @Override
  @Transactional(readOnly = true)
  public LoanKpiResponse getLoanKpis() {
    return getLoanKpisUseCase.execute();
  }

  @Override
  @Transactional(readOnly = true)
  public byte[] exportLoanKpis() {
    LoanKpiResponse kpis = getLoanKpisUseCase.execute();
    return excelExportService.exportLoanKpis(kpis);
  }

  @Override
  @Transactional(readOnly = true)
  public SlaReportResponse getSlaReport(UUID loanId) {
    return getSlaReportUseCase.execute(loanId);
  }

  @Override
  @Transactional(readOnly = true)
  public byte[] exportSlaReport(UUID loanId) {
    SlaReportResponse slaReport = getSlaReportUseCase.execute(loanId);
    return excelExportService.exportSlaReport(slaReport);
  }
}
