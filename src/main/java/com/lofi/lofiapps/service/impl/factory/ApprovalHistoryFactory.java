package com.lofi.lofiapps.service.impl.factory;

import com.lofi.lofiapps.entity.ApprovalHistory;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.repository.ApprovalHistoryRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApprovalHistoryFactory {

  private final ApprovalHistoryRepository approvalHistoryRepository;

  public ApprovalHistory recordStatusChange(
      UUID loanId, LoanStatus fromStatus, LoanStatus toStatus, String actionBy, String notes) {
    ApprovalHistory history =
        ApprovalHistory.builder()
            .loanId(loanId)
            .fromStatus(fromStatus)
            .toStatus(toStatus)
            .actionBy(actionBy)
            .notes(notes)
            .createdAt(LocalDateTime.now())
            .build();

    return approvalHistoryRepository.save(history);
  }

  public ApprovalHistory recordStatusChange(
      Loan loan, LoanStatus fromStatus, LoanStatus toStatus, String actionBy, String notes) {
    return recordStatusChange(loan.getId(), fromStatus, toStatus, actionBy, notes);
  }

  public ApprovalHistory recordSubmission(UUID loanId, String submittedBy) {
    return recordStatusChange(
        loanId, LoanStatus.DRAFT, LoanStatus.SUBMITTED, submittedBy, "Loan submitted by customer");
  }

  public ApprovalHistory recordReview(UUID loanId, String reviewedBy, String notes) {
    return recordStatusChange(loanId, LoanStatus.SUBMITTED, LoanStatus.REVIEWED, reviewedBy, notes);
  }

  public ApprovalHistory recordApproval(UUID loanId, String approvedBy, String notes) {
    return recordStatusChange(loanId, LoanStatus.REVIEWED, LoanStatus.APPROVED, approvedBy, notes);
  }

  public ApprovalHistory recordRejection(UUID loanId, String rejectedBy, String reason) {
    return recordStatusChange(
        loanId,
        LoanStatus.REVIEWED,
        LoanStatus.REJECTED,
        rejectedBy,
        reason != null ? reason : "Loan rejected");
  }

  public ApprovalHistory recordCancellation(UUID loanId, String cancelledBy, String reason) {
    return recordStatusChange(
        loanId,
        null, // From status can vary
        LoanStatus.CANCELLED,
        cancelledBy,
        reason != null ? reason : "Loan cancelled");
  }

  public ApprovalHistory recordDisbursement(UUID loanId, String disbursedBy, String notes) {
    return recordStatusChange(
        loanId, LoanStatus.APPROVED, LoanStatus.DISBURSED, disbursedBy, notes);
  }

  public ApprovalHistory recordCompletion(UUID loanId, String completedBy, String notes) {
    return recordStatusChange(
        loanId, LoanStatus.DISBURSED, LoanStatus.COMPLETED, completedBy, notes);
  }
}
