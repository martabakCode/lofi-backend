package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.dto.request.DisbursementRequest;
import com.lofi.lofiapps.dto.request.LoanCriteria;
import com.lofi.lofiapps.dto.request.LoanRequest;
import com.lofi.lofiapps.dto.request.RejectLoanRequest;
import com.lofi.lofiapps.dto.request.ReviewLoanRequest;
import com.lofi.lofiapps.dto.response.*;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.security.idempotency.RequireIdempotency;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Loan Management Endpoints")
public class LoanController {
  private final LoanService loanService;

  @GetMapping
  @Operation(summary = "Get Loans with pagination and filters")
  public ResponseEntity<ApiResponse<PagedResponse<LoanResponse>>> getLoans(
      @RequestParam(required = false) LoanStatus status,
      @RequestParam(required = false) UUID branchId,
      @RequestParam(required = false) UUID customerId,
      @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
          Pageable pageable) {

    LoanCriteria criteria =
        LoanCriteria.builder().status(status).branchId(branchId).customerId(customerId).build();

    return ResponseEntity.ok(ApiResponse.success(loanService.getLoans(criteria, pageable)));
  }

  @GetMapping("/me")
  @PreAuthorize("hasRole('CUSTOMER')")
  @Operation(summary = "Get my loans")
  public ResponseEntity<ApiResponse<PagedResponse<LoanResponse>>> getMyLoans(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(
        ApiResponse.success(loanService.getMyLoans(userPrincipal.getId(), pageable)));
  }

  @GetMapping("/history")
  @PreAuthorize("hasRole('CUSTOMER')")
  @Operation(summary = "Get my loan history")
  public ResponseEntity<ApiResponse<PagedResponse<LoanResponse>>> getLoanHistory(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(
        ApiResponse.success(loanService.getLoanHistory(userPrincipal.getId(), pageable)));
  }

  @PostMapping("/{id}/submit")
  @PreAuthorize("hasRole('CUSTOMER')")
  @Operation(summary = "Submit a loan application")
  public ResponseEntity<ApiResponse<LoanResponse>> submitLoan(
      @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            loanService.submitLoan(id, userPrincipal.getUsername()),
            "Loan submitted successfully"));
  }

  @PostMapping("/{id}/review")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('MARKETING')")
  @Operation(summary = "Review a loan application")
  public ResponseEntity<ApiResponse<LoanResponse>> reviewLoan(
      @PathVariable UUID id,
      @RequestBody ReviewLoanRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            loanService.reviewLoan(id, userPrincipal.getUsername(), request.getNotes()),
            "Loan reviewed successfully"));
  }

  @PostMapping("/{id}/approve")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('BRANCH_MANAGER')")
  @RequireIdempotency(ttlHours = 24)
  @Operation(summary = "Approve a loan")
  public ResponseEntity<ApiResponse<LoanResponse>> approveLoan(
      @PathVariable UUID id,
      @Valid @RequestBody(required = false) ReviewLoanRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    String notes =
        (request != null && StringUtils.hasText(request.getNotes()))
            ? request.getNotes()
            : "Approved by Branch Manager";
    return ResponseEntity.ok(
        ApiResponse.success(
            loanService.approveLoan(id, userPrincipal.getUsername(), notes),
            "Loan approved successfully"));
  }

  @PostMapping("/{id}/reject")
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('MARKETING') or hasRole('BRANCH_MANAGER')")
  @RequireIdempotency(ttlHours = 24)
  @Operation(summary = "Reject a loan")
  public ResponseEntity<ApiResponse<LoanResponse>> rejectLoan(
      @PathVariable UUID id,
      @Valid @RequestBody RejectLoanRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            loanService.rejectLoan(id, userPrincipal.getUsername(), request.getReason()),
            "Loan rejected successfully"));
  }

  @PostMapping("/{id}/cancel")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CUSTOMER')")
  @RequireIdempotency(ttlHours = 24)
  @Operation(summary = "Cancel a loan")
  public ResponseEntity<ApiResponse<LoanResponse>> cancelLoan(
      @PathVariable UUID id,
      @Valid @RequestBody RejectLoanRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            loanService.cancelLoan(id, userPrincipal.getUsername(), request.getReason()),
            "Loan cancelled successfully"));
  }

  @PostMapping("/{id}/rollback")
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('MARKETING') or hasRole('BRANCH_MANAGER')")
  @Operation(summary = "Rollback a loan status")
  public ResponseEntity<ApiResponse<LoanResponse>> rollbackLoan(
      @PathVariable UUID id,
      @RequestBody ReviewLoanRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            loanService.rollbackLoan(id, userPrincipal.getUsername(), request.getNotes()),
            "Loan rolled back successfully"));
  }

  @PostMapping("/{id}/disburse")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('BACKOFFICE')")
  @RequireIdempotency(ttlHours = 24)
  @Operation(summary = "Disburse a loan")
  public ResponseEntity<ApiResponse<LoanResponse>> disburseLoan(
      @PathVariable UUID id,
      @Valid @RequestBody DisbursementRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    // Note: service.disburseLoan expects notes/reference in last param
    return ResponseEntity.ok(
        ApiResponse.success(
            loanService.disburseLoan(id, userPrincipal.getUsername(), request.getReferenceNumber()),
            "Loan disbursed successfully"));
  }

  @PostMapping("/{id}/complete")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('BACKOFFICE')")
  @Operation(summary = "Complete a loan")
  public ResponseEntity<ApiResponse<LoanResponse>> completeLoan(
      @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            loanService.completeLoan(id, userPrincipal.getUsername()),
            "Loan completed successfully"));
  }

  @PostMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  @Operation(summary = "Apply for a new loan")
  public ResponseEntity<ApiResponse<LoanResponse>> applyLoan(
      @Valid @RequestBody LoanRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            loanService.applyLoan(request, userPrincipal.getId(), userPrincipal.getUsername()),
            "Loan applied successfully"));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get loan details")
  public ResponseEntity<ApiResponse<LoanResponse>> getLoanDetail(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(loanService.getLoanDetail(id)));
  }

  @GetMapping("/{id}/analysis")
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('MARKETING') or hasRole('BRANCH_MANAGER')")
  @Operation(summary = "Get AI Analysis for a loan")
  public ResponseEntity<ApiResponse<LoanAnalysisResponse>> analyzeLoan(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(loanService.analyzeLoan(id)));
  }

  @GetMapping("/{id}/analysis/branch-support")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('BRANCH_MANAGER')")
  @Operation(summary = "Get AI Branch Decision Support for a loan")
  public ResponseEntity<ApiResponse<BranchManagerSupportResponse>> analyzeLoanBranchSupport(
      @PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(loanService.analyzeLoanBranchSupport(id)));
  }

  @GetMapping("/{id}/analysis/risk-evaluation")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('BACKOFFICE')")
  @Operation(summary = "Get AI Risk Evaluation for Back Office")
  public ResponseEntity<ApiResponse<BackOfficeRiskEvaluationResponse>> analyzeRiskEvaluation(
      @PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(loanService.analyzeBackOfficeRiskEvaluation(id)));
  }
}
