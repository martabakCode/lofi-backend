package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.model.dto.request.LoanCriteria;
import com.lofi.lofiapps.model.dto.request.LoanRequest;
import com.lofi.lofiapps.model.dto.response.*;
import com.lofi.lofiapps.model.dto.response.LoanResponse;
import com.lofi.lofiapps.model.dto.response.PagedResponse;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.impl.loan.ApplyLoanUseCase;
import com.lofi.lofiapps.service.impl.loan.ApproveLoanUseCase;
import com.lofi.lofiapps.service.impl.loan.CompleteLoanUseCase;
import com.lofi.lofiapps.service.impl.loan.DisburseLoanUseCase;
import com.lofi.lofiapps.service.impl.loan.GetLoanDetailUseCase;
import com.lofi.lofiapps.service.impl.loan.GetLoansUseCase;
import com.lofi.lofiapps.service.impl.loan.RejectLoanUseCase;
import com.lofi.lofiapps.service.impl.loan.ReviewLoanUseCase;
import com.lofi.lofiapps.service.impl.loan.RollbackLoanUseCase;
import com.lofi.lofiapps.service.impl.loan.SubmitLoanUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Loans", description = "Loan Management Endpoints")
public class LoanController {
  private final ApplyLoanUseCase applyLoanUseCase;
  private final GetLoansUseCase getLoansUseCase;
  private final GetLoanDetailUseCase getLoanDetailUseCase;
  private final ApproveLoanUseCase approveLoanUseCase;
  private final RejectLoanUseCase rejectLoanUseCase;
  private final DisburseLoanUseCase disburseLoanUseCase;
  private final ReviewLoanUseCase reviewLoanUseCase;
  private final RollbackLoanUseCase rollbackLoanUseCase;
  private final SubmitLoanUseCase submitLoanUseCase;
  private final CompleteLoanUseCase completeLoanUseCase;

  @GetMapping
  @io.swagger.v3.oas.annotations.Operation(summary = "Get Loans with pagination and filters")
  public ResponseEntity<ApiResponse<PagedResponse<LoanResponse>>> getLoans(
      @RequestParam(required = false) com.lofi.lofiapps.model.enums.LoanStatus status,
      @RequestParam(required = false) UUID branchId,
      @RequestParam(required = false) UUID customerId,
      @org.springframework.data.web.PageableDefault(
              size = 10,
              sort = "createdAt",
              direction = org.springframework.data.domain.Sort.Direction.DESC)
          org.springframework.data.domain.Pageable pageable) {

    LoanCriteria criteria =
        LoanCriteria.builder().status(status).branchId(branchId).customerId(customerId).build();

    return ResponseEntity.ok(ApiResponse.success(getLoansUseCase.execute(criteria, pageable)));
  }

  @PostMapping("/{id}/submit")
  @PreAuthorize("hasRole('CUSTOMER')")
  @io.swagger.v3.oas.annotations.Operation(summary = "Submit a loan application")
  public ResponseEntity<ApiResponse<LoanResponse>> submitLoan(
      @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            submitLoanUseCase.execute(id, userPrincipal.getId()), "Loan submitted successfully"));
  }

  @PostMapping("/{id}/review")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('MARKETING')")
  @io.swagger.v3.oas.annotations.Operation(summary = "Review a loan application")
  public ResponseEntity<ApiResponse<LoanResponse>> reviewLoan(
      @PathVariable UUID id,
      @RequestBody com.lofi.lofiapps.model.dto.request.ReviewLoanRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            reviewLoanUseCase.execute(id, userPrincipal.getUsername(), request.getNotes()),
            "Loan reviewed successfully"));
  }

  @PostMapping("/{id}/approve")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('BRANCH_MANAGER')")
  @io.swagger.v3.oas.annotations.Operation(summary = "Approve a loan")
  public ResponseEntity<ApiResponse<LoanResponse>> approveLoan(
      @PathVariable UUID id,
      @Valid @RequestBody(required = false)
          com.lofi.lofiapps.model.dto.request.ReviewLoanRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    String notes =
        (request != null && StringUtils.hasText(request.getNotes()))
            ? request.getNotes()
            : "Approved by Branch Manager";
    return ResponseEntity.ok(
        ApiResponse.success(
            approveLoanUseCase.execute(id, userPrincipal.getUsername(), notes),
            "Loan approved successfully"));
  }

  @PostMapping("/{id}/reject")
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('MARKETING') or hasRole('BRANCH_MANAGER')")
  @io.swagger.v3.oas.annotations.Operation(summary = "Reject a loan")
  public ResponseEntity<ApiResponse<LoanResponse>> rejectLoan(
      @PathVariable UUID id,
      @Valid @RequestBody com.lofi.lofiapps.model.dto.request.RejectLoanRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            rejectLoanUseCase.execute(id, userPrincipal.getUsername(), request.getReason(), false),
            "Loan rejected successfully"));
  }

  @PostMapping("/{id}/cancel")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CUSTOMER')")
  @io.swagger.v3.oas.annotations.Operation(summary = "Cancel a loan")
  public ResponseEntity<ApiResponse<LoanResponse>> cancelLoan(
      @PathVariable UUID id,
      @Valid @RequestBody com.lofi.lofiapps.model.dto.request.RejectLoanRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            rejectLoanUseCase.execute(id, userPrincipal.getUsername(), request.getReason(), true),
            "Loan cancelled successfully"));
  }

  @PostMapping("/{id}/rollback")
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('MARKETING') or hasRole('BRANCH_MANAGER')")
  @io.swagger.v3.oas.annotations.Operation(summary = "Rollback a loan status")
  public ResponseEntity<ApiResponse<LoanResponse>> rollbackLoan(
      @PathVariable UUID id,
      @RequestBody com.lofi.lofiapps.model.dto.request.ReviewLoanRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            rollbackLoanUseCase.execute(id, userPrincipal.getUsername(), request.getNotes()),
            "Loan rolled back successfully"));
  }

  @PostMapping("/{id}/disburse")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('BACKOFFICE')")
  @io.swagger.v3.oas.annotations.Operation(summary = "Disburse a loan")
  public ResponseEntity<ApiResponse<LoanResponse>> disburseLoan(
      @PathVariable UUID id,
      @Valid @RequestBody com.lofi.lofiapps.model.dto.request.DisbursementRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            disburseLoanUseCase.execute(id, request, userPrincipal.getUsername()),
            "Loan disbursed successfully"));
  }

  @PostMapping("/{id}/complete")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('BACKOFFICE')")
  @io.swagger.v3.oas.annotations.Operation(summary = "Complete a loan")
  public ResponseEntity<ApiResponse<LoanResponse>> completeLoan(
      @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            completeLoanUseCase.execute(id, userPrincipal.getUsername()),
            "Loan completed successfully"));
  }

  @PostMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  @io.swagger.v3.oas.annotations.Operation(summary = "Apply for a new loan")
  public ResponseEntity<ApiResponse<LoanResponse>> applyLoan(
      @Valid @RequestBody LoanRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return ResponseEntity.ok(
        ApiResponse.success(
            applyLoanUseCase.execute(request, userPrincipal.getId()), "Loan applied successfully"));
  }

  @GetMapping("/{id}")
  @io.swagger.v3.oas.annotations.Operation(summary = "Get loan details")
  public ResponseEntity<ApiResponse<LoanResponse>> getLoanDetail(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(getLoanDetailUseCase.execute(id)));
  }
}
