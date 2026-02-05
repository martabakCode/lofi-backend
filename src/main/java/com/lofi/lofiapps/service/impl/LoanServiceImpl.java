package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.dto.request.LoanCriteria;
import com.lofi.lofiapps.dto.request.LoanRequest;
import com.lofi.lofiapps.dto.response.BackOfficeRiskEvaluationResponse;
import com.lofi.lofiapps.dto.response.BranchManagerSupportResponse;
import com.lofi.lofiapps.dto.response.LoanAnalysisResponse;
import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.dto.response.MarketingLoanReviewResponse;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.service.LoanService;
import com.lofi.lofiapps.service.impl.usecase.loan.AnalyzeLoanUseCase;
import com.lofi.lofiapps.service.impl.usecase.loan.ApplyLoanUseCase;
import com.lofi.lofiapps.service.impl.usecase.loan.ApproveLoanUseCase;
import com.lofi.lofiapps.service.impl.usecase.loan.BackOfficeRiskEvaluationUseCase;
import com.lofi.lofiapps.service.impl.usecase.loan.BranchManagerSupportUseCase;
import com.lofi.lofiapps.service.impl.usecase.loan.CancelLoanUseCase;
import com.lofi.lofiapps.service.impl.usecase.loan.CompleteLoanUseCase;
import com.lofi.lofiapps.service.impl.usecase.loan.DisburseLoanUseCase;
import com.lofi.lofiapps.service.impl.usecase.loan.GetLoanDetailUseCase;
import com.lofi.lofiapps.service.impl.usecase.loan.GetLoansUseCase;
import com.lofi.lofiapps.service.impl.usecase.loan.MarketingReviewLoanUseCase;
import com.lofi.lofiapps.service.impl.usecase.loan.RejectLoanUseCase;
import com.lofi.lofiapps.service.impl.usecase.loan.ReviewLoanUseCase;
import com.lofi.lofiapps.service.impl.usecase.loan.RollbackLoanUseCase;
import com.lofi.lofiapps.service.impl.usecase.loan.SubmitLoanUseCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

  private final ApplyLoanUseCase applyLoanUseCase;
  private final GetLoansUseCase getLoansUseCase;
  private final GetLoanDetailUseCase getLoanDetailUseCase;
  private final ApproveLoanUseCase approveLoanUseCase;
  private final RejectLoanUseCase rejectLoanUseCase;
  private final CancelLoanUseCase cancelLoanUseCase;
  private final DisburseLoanUseCase disburseLoanUseCase;
  private final ReviewLoanUseCase reviewLoanUseCase;
  private final RollbackLoanUseCase rollbackLoanUseCase;
  private final SubmitLoanUseCase submitLoanUseCase;
  private final CompleteLoanUseCase completeLoanUseCase;
  private final com.lofi.lofiapps.service.impl.usecase.loan.MarketingApplyLoanUseCase
      marketingApplyLoanUseCase;
  private final com.lofi.lofiapps.service.impl.usecase.loan.DraftLoanUseCase draftLoanUseCase;
  private final com.lofi.lofiapps.service.impl.usecase.loan.MarketingDraftLoanUseCase
      marketingDraftLoanUseCase;

  // AI / Analysis UseCases
  private final AnalyzeLoanUseCase analyzeLoanUseCase;
  private final MarketingReviewLoanUseCase marketingReviewLoanUseCase;
  private final BackOfficeRiskEvaluationUseCase backOfficeRiskEvaluationUseCase;
  private final BranchManagerSupportUseCase branchManagerSupportUseCase;

  @Override
  public LoanResponse applyLoan(LoanRequest request, UUID userId, String username) {
    return applyLoanUseCase.execute(request, userId, username);
  }

  @Override
  public LoanResponse draftLoan(LoanRequest request, UUID userId, String username) {
    return draftLoanUseCase.execute(request, userId, username);
  }

  @Override
  public LoanResponse marketingApplyLoan(
      com.lofi.lofiapps.dto.request.MarketingApplyLoanRequest request, String marketingUsername) {
    return marketingApplyLoanUseCase.execute(request, marketingUsername);
  }

  @Override
  public LoanResponse marketingDraftLoan(
      com.lofi.lofiapps.dto.request.MarketingApplyLoanRequest request, String marketingUsername) {
    return marketingDraftLoanUseCase.execute(request, marketingUsername);
  }

  @Override
  public PagedResponse<LoanResponse> getLoans(LoanCriteria criteria, Pageable pageable) {
    return getLoansUseCase.execute(criteria, pageable);
  }

  @Override
  public PagedResponse<LoanResponse> getMyLoans(UUID customerId, Pageable pageable) {
    // Active loans should exclude DRAFT and CANCELLED statuses
    LoanCriteria criteria =
        LoanCriteria.builder()
            .customerId(customerId)
            .excludeStatuses(List.of(LoanStatus.DRAFT, LoanStatus.CANCELLED))
            .build();
    return getLoansUseCase.execute(criteria, pageable);
  }

  @Override
  public PagedResponse<LoanResponse> getLoanHistory(UUID customerId, Pageable pageable) {
    // Loan history should show all loans including DRAFT and CANCELLED
    LoanCriteria criteria = LoanCriteria.builder().customerId(customerId).build();
    return getLoansUseCase.execute(criteria, pageable);
  }

  @Override
  public LoanResponse getLoanDetail(UUID loanId) {
    return getLoanDetailUseCase.execute(loanId);
  }

  @Override
  public LoanResponse approveLoan(UUID loanId, String approverUsername, String notes) {
    return approveLoanUseCase.execute(loanId, approverUsername, notes);
  }

  @Override
  public LoanResponse rejectLoan(UUID loanId, String rejectorUsername, String notes) {
    return rejectLoanUseCase.execute(loanId, rejectorUsername, notes);
  }

  @Override
  public LoanResponse cancelLoan(UUID loanId, String cancellerUsername, String reason) {
    return cancelLoanUseCase.execute(loanId, cancellerUsername, reason);
  }

  @Override
  public LoanResponse disburseLoan(UUID loanId, String officerUsername, String notes) {
    return disburseLoanUseCase.execute(loanId, officerUsername, notes);
  }

  @Override
  public LoanResponse reviewLoan(UUID loanId, String reviewerUsername, String notes) {
    return reviewLoanUseCase.execute(loanId, reviewerUsername, notes);
  }

  @Override
  public LoanResponse rollbackLoan(UUID loanId, String officerUsername, String notes) {
    return rollbackLoanUseCase.execute(loanId, officerUsername, notes);
  }

  @Override
  public LoanResponse submitLoan(UUID loanId, String username) {
    return submitLoanUseCase.execute(loanId, username);
  }

  @Override
  public LoanResponse completeLoan(UUID loanId, String username) {
    return completeLoanUseCase.execute(loanId, username);
  }

  @Override
  public LoanAnalysisResponse analyzeLoan(UUID loanId) {
    return analyzeLoanUseCase.execute(loanId);
  }

  @Override
  public MarketingLoanReviewResponse marketingReviewLoan(UUID loanId) {
    return marketingReviewLoanUseCase.execute(loanId);
  }

  @Override
  public BackOfficeRiskEvaluationResponse analyzeBackOfficeRiskEvaluation(UUID loanId) {
    return backOfficeRiskEvaluationUseCase.execute(loanId);
  }

  @Override
  public BranchManagerSupportResponse analyzeLoanBranchSupport(UUID loanId) {
    return branchManagerSupportUseCase.execute(loanId);
  }
}
