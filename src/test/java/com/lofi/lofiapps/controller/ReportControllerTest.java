package com.lofi.lofiapps.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.lofi.lofiapps.dto.response.LoanKpiResponse;
import com.lofi.lofiapps.dto.response.SlaReportResponse;
import com.lofi.lofiapps.service.ReportService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportControllerTest {

  private MockMvc mockMvc;

  @Mock private ReportService reportService;

  @InjectMocks private ReportController reportController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
  }

  @Test
  @DisplayName("Get KPIs should return KPI data")
  void getKpis_ShouldReturnKpiData() throws Exception {
    when(reportService.getLoanKpis()).thenReturn(LoanKpiResponse.builder().build());

    mockMvc.perform(get("/reports/kpis")).andExpect(status().isOk());

    verify(reportService, times(1)).getLoanKpis();
  }

  @Test
  @DisplayName("Export KPIs should return file")
  void exportKpis_ShouldReturnFile() throws Exception {
    byte[] fileContent = "kpi-data".getBytes();
    when(reportService.exportLoanKpis()).thenReturn(fileContent);

    mockMvc
        .perform(get("/reports/kpis/export"))
        .andExpect(status().isOk())
        .andExpect(
            header().string("Content-Disposition", "attachment; filename=\"loan-kpis.xlsx\""))
        .andExpect(content().bytes(fileContent));

    verify(reportService, times(1)).exportLoanKpis();
  }

  @Test
  @DisplayName("Get SLA report should return SLA data")
  void getSlaReport_ShouldReturnSlaData() throws Exception {
    UUID loanId = UUID.randomUUID();
    when(reportService.getSlaReport(loanId)).thenReturn(SlaReportResponse.builder().build());

    mockMvc.perform(get("/reports/sla/{loanId}", loanId)).andExpect(status().isOk());

    verify(reportService, times(1)).getSlaReport(loanId);
  }

  @Test
  @DisplayName("Export SLA report should return file")
  void exportSlaReport_ShouldReturnFile() throws Exception {
    UUID loanId = UUID.randomUUID();
    byte[] fileContent = "sla-data".getBytes();
    when(reportService.exportSlaReport(loanId)).thenReturn(fileContent);

    mockMvc
        .perform(get("/reports/sla/{loanId}/export", loanId))
        .andExpect(status().isOk())
        .andExpect(
            header()
                .string(
                    "Content-Disposition",
                    "attachment; filename=\"sla-report-" + loanId + ".xlsx\""))
        .andExpect(content().bytes(fileContent));

    verify(reportService, times(1)).exportSlaReport(loanId);
  }
}
