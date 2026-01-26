package com.lofi.lofiapps.service.impl.export;

import com.lofi.lofiapps.dto.response.LoanKpiResponse;
import com.lofi.lofiapps.dto.response.SlaReportResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExcelExportService {

  public byte[] exportLoanKpis(LoanKpiResponse data) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {

      Sheet sheet = workbook.createSheet("Loan KPIs");

      // Header Row
      Row headerRow = sheet.createRow(0);
      String[] columns = { "Metric", "Value" };

      CellStyle headerStyle = createHeaderStyle(workbook);

      for (int i = 0; i < columns.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(columns[i]);
        cell.setCellStyle(headerStyle);
      }

      // Data Rows
      int rowNum = 1;

      addKpiRow(sheet, rowNum++, "Total Loans", data.getTotalLoans());
      addKpiRow(sheet, rowNum++, "Total Submitted", data.getTotalSubmitted());
      addKpiRow(sheet, rowNum++, "Total Reviewed", data.getTotalReviewed());
      addKpiRow(sheet, rowNum++, "Total Approved", data.getTotalApproved());
      addKpiRow(sheet, rowNum++, "Total Rejected", data.getTotalRejected());
      addKpiRow(sheet, rowNum++, "Total Cancelled", data.getTotalCancelled());
      addKpiRow(sheet, rowNum++, "Total Disbursed", data.getTotalDisbursed());
      addKpiRow(sheet, rowNum++, "Total Completed", data.getTotalCompleted());

      // Product Breakdown
      rowNum++; // Spacer
      Row productHeader = sheet.createRow(rowNum++);
      Cell phCell = productHeader.createCell(0);
      phCell.setCellValue("Loans by Product");
      phCell.setCellStyle(headerStyle);

      for (Map.Entry<String, Long> entry : data.getLoansByProduct().entrySet()) {
        addKpiRow(sheet, rowNum++, entry.getKey(), entry.getValue());
      }

      // Auto-size columns
      for (int i = 0; i < columns.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return out.toByteArray();
    } catch (IOException e) {
      log.error("Failed to export KPIs to Excel", e);
      throw new RuntimeException("Failed to export Excel file", e);
    }
  }

  public byte[] exportSlaReport(SlaReportResponse data) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {

      Sheet sheet = workbook.createSheet("SLA Report");

      // Loan Info
      Row infoRow = sheet.createRow(0);
      infoRow.createCell(0).setCellValue("Loan ID:");
      infoRow.createCell(1).setCellValue(data.getLoanId().toString());

      Row infoRow2 = sheet.createRow(1);
      infoRow2.createCell(0).setCellValue("Customer:");
      infoRow2.createCell(1).setCellValue(data.getCustomerName());

      Row infoRow3 = sheet.createRow(2);
      infoRow3.createCell(0).setCellValue("Total Duration (Mins):");
      infoRow3.createCell(1).setCellValue(data.getTotalDurationMinutes());

      // Header Row
      Row headerRow = sheet.createRow(4);
      String[] columns = { "Stage", "Status", "Action By", "Duration (Mins)" };
      CellStyle headerStyle = createHeaderStyle(workbook);

      for (int i = 0; i < columns.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(columns[i]);
        cell.setCellStyle(headerStyle);
      }

      // Data Rows
      int rowNum = 5;
      for (SlaReportResponse.StageSlaInfo stage : data.getStages()) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(stage.getStage());
        row.createCell(1).setCellValue(stage.getStatus());
        row.createCell(2).setCellValue(stage.getActionBy());
        row.createCell(3).setCellValue(stage.getDurationMinutes());
      }

      // Auto-size columns
      for (int i = 0; i < columns.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return out.toByteArray();
    } catch (IOException e) {
      log.error("Failed to export SLA Report to Excel", e);
      throw new RuntimeException("Failed to export Excel file", e);
    }
  }

  private void addKpiRow(Sheet sheet, int rowNum, String label, long value) {
    Row row = sheet.createRow(rowNum);
    row.createCell(0).setCellValue(label);
    row.createCell(1).setCellValue(value);
  }

  private CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);
    return style;
  }
}
