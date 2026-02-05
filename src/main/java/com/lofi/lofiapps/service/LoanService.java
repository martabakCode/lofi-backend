package com.lofi.lofiapps.service;

import com.lofi.lofiapps.dto.request.LoanCriteria;
import com.lofi.lofiapps.dto.request.LoanRequest;
import com.lofi.lofiapps.dto.response.*;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface LoanService {
  LoanResponse applyLoan(LoanRequest request, UUID userId, String username);

  LoanResponse draftLoan(LoanRequest request, UUID userId, String username);

  LoanResponse marketingApplyLoan(
      com.lofi.lofiapps.dto.request.MarketingApplyLoanRequest request, String marketingUsername);

  LoanResponse marketingDraftLoan(
      com.lofi.lofiapps.dto.request.MarketingApplyLoanRequest request, String marketingUsername);

  PagedResponse<LoanResponse> getLoans(LoanCriteria criteria, Pageable pageable);

  PagedResponse<LoanResponse> getMyLoans(UUID customerId, Pageable pageable);

  PagedResponse<LoanResponse> getLoanHistory(UUID customerId, Pageable pageable);

  LoanResponse getLoanDetail(UUID loanId);

  LoanResponse approveLoan(UUID loanId, String approverUsername, String notes);

  LoanResponse rejectLoan(UUID loanId, String rejectorUsername, String notes);

  LoanResponse cancelLoan(UUID loanId, String cancellerUsername, String reason);

  LoanResponse disburseLoan(UUID loanId, String officerUsername, String notes);

  LoanResponse reviewLoan(UUID loanId, String reviewerUsername, String notes);

  LoanResponse rollbackLoan(UUID loanId, String officerUsername, String notes);

  LoanResponse submitLoan(UUID loanId, String username);

  LoanResponse completeLoan(UUID loanId, String username);

  // AI / Analysis
  LoanAnalysisResponse analyzeLoan(UUID loanId);

  MarketingLoanReviewResponse marketingReviewLoan(UUID loanId);

  BackOfficeRiskEvaluationResponse analyzeBackOfficeRiskEvaluation(UUID loanId);

  BranchManagerSupportResponse analyzeLoanBranchSupport(UUID loanId);
}
