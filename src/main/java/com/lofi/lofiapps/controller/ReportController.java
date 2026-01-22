package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.model.dto.response.LoanKpiResponse;
import com.lofi.lofiapps.model.dto.response.SlaReportResponse;
import com.lofi.lofiapps.service.impl.report.GetLoanKpisUseCase;
import com.lofi.lofiapps.service.impl.report.GetSlaReportUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "KPI and SLA Report Endpoints")
public class ReportController {
  private final GetLoanKpisUseCase getLoanKpisUseCase;
  private final GetSlaReportUseCase getSlaReportUseCase;
  private final com.lofi.lofiapps.service.impl.export.ExcelExportService excelExportService;

  @GetMapping("/kpis")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @Operation(summary = "Get Loan KPIs")
  public ResponseEntity<LoanKpiResponse> getKpis() {
    return ResponseEntity.ok(getLoanKpisUseCase.execute());
  }

  @GetMapping("/kpis/export")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @Operation(summary = "Export Loan KPIs to Excel")
  public ResponseEntity<byte[]> exportKpis() {
    LoanKpiResponse kpis = getLoanKpisUseCase.execute();
    byte[] excelFile = excelExportService.exportLoanKpis(kpis);

    return ResponseEntity.ok()
        .header(
            org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"loan-kpis.xlsx\"")
        .contentType(
            org.springframework.http.MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(excelFile);
  }

  @GetMapping("/sla/{loanId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MARKETING', 'BRANCH_MANAGER')")
  @Operation(summary = "Get SLA Report for a specific loan")
  public ResponseEntity<SlaReportResponse> getSlaReport(@PathVariable UUID loanId) {
    return ResponseEntity.ok(getSlaReportUseCase.execute(loanId));
  }

  @GetMapping("/sla/{loanId}/export")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MARKETING', 'BRANCH_MANAGER')")
  @Operation(summary = "Export SLA Report for a specific loan to Excel")
  public ResponseEntity<byte[]> exportSlaReport(@PathVariable UUID loanId) {
    SlaReportResponse slaReport = getSlaReportUseCase.execute(loanId);
    byte[] excelFile = excelExportService.exportSlaReport(slaReport);

    return ResponseEntity.ok()
        .header(
            org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"sla-report-" + loanId + ".xlsx\"")
        .contentType(
            org.springframework.http.MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(excelFile);
  }
}
