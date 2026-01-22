package com.lofi.lofiapps.service.impl.loan;

import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.model.dto.response.LoanResponse;
import com.lofi.lofiapps.model.entity.ApprovalHistory;
import com.lofi.lofiapps.model.entity.Loan;
import com.lofi.lofiapps.model.enums.ApprovalStage;
import com.lofi.lofiapps.model.enums.LoanStatus;
import com.lofi.lofiapps.repository.ApprovalHistoryRepository;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.service.NotificationService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApproveLoanUseCase {
  private final LoanRepository loanRepository;
  private final ApprovalHistoryRepository approvalHistoryRepository;
  private final NotificationService notificationService;
  private final LoanDtoMapper loanDtoMapper;
  private final com.lofi.lofiapps.repository.UserRepository userRepository;
  private final com.lofi.lofiapps.service.RoleActionGuard roleActionGuard;
  private final com.lofi.lofiapps.service.BranchAccessGuard branchAccessGuard;
  private final com.lofi.lofiapps.service.LoanActionValidator loanActionValidator;

  @Transactional
  public LoanResponse execute(UUID loanId, String actionBy, String notes) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId.toString()));

    com.lofi.lofiapps.model.entity.User user =
        userRepository
            .findByEmail(actionBy)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", actionBy));

    // Guards
    roleActionGuard.validate(user, "approve");
    branchAccessGuard.validate(user, loan);
    loanActionValidator.validate(loan, "approve");

    // Validation moved to LoanActionValidator
    // if (loan.getLoanStatus() != LoanStatus.REVIEWED) {
    // throw new IllegalStateException("Only reviewed loans can be approved by
    // Branch Manager");
    // }

    // Check if customer already has an APPROVED or DISBURSED or COMPLETED loan
    if (loan.getCustomer() == null) {
      throw new IllegalStateException(
          "Loan (ID: " + loanId + ") does not have a customer assigned.");
    }
    UUID customerId = loan.getCustomer().getId();
    boolean hasApprovedLoan =
        loanRepository.findByCustomerId(customerId).stream()
            .anyMatch(
                l ->
                    l.getLoanStatus() == LoanStatus.APPROVED
                        || l.getLoanStatus() == LoanStatus.DISBURSED
                        || l.getLoanStatus() == LoanStatus.COMPLETED);

    if (hasApprovedLoan) {
      throw new IllegalStateException("Customer already has an active or approved loan");
    }

    LoanStatus fromStatus = loan.getLoanStatus();
    loan.setLoanStatus(LoanStatus.APPROVED);
    loan.setCurrentStage(ApprovalStage.BACKOFFICE);
    loan.setApprovedAt(LocalDateTime.now());
    loan.setLastStatusChangedAt(LocalDateTime.now());

    Loan savedLoan = loanRepository.save(loan);

    // Save history
    approvalHistoryRepository.save(
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(fromStatus)
            .toStatus(LoanStatus.APPROVED)
            .actionBy(actionBy)
            .notes(notes)
            .build());

    // Auto-cancel other active loans for this customer
    cancelOtherActiveLoans(customerId, loan.getId(), actionBy);

    // Notify customer
    notificationService.notifyLoanStatusChange(customerId, LoanStatus.APPROVED);

    return loanDtoMapper.toResponse(savedLoan);
  }

  private void cancelOtherActiveLoans(UUID customerId, UUID approvedLoanId, String actionBy) {
    loanRepository.findByCustomerId(customerId).stream()
        .filter(l -> !l.getId().equals(approvedLoanId))
        .filter(
            l ->
                l.getLoanStatus() == LoanStatus.SUBMITTED
                    || l.getLoanStatus() == LoanStatus.REVIEWED
                    || l.getLoanStatus() == LoanStatus.DRAFT)
        .forEach(
            l -> {
              LoanStatus oldStatus = l.getLoanStatus();
              l.setLoanStatus(LoanStatus.CANCELLED);
              l.setLastStatusChangedAt(LocalDateTime.now());
              loanRepository.save(l);

              approvalHistoryRepository.save(
                  ApprovalHistory.builder()
                      .loanId(l.getId())
                      .fromStatus(oldStatus)
                      .toStatus(LoanStatus.CANCELLED)
                      .actionBy("SYSTEM")
                      .notes("Auto-cancelled because another loan was approved")
                      .build());

              if (l.getCustomer() != null) {
                notificationService.notifyLoanStatusChange(
                    l.getCustomer().getId(), LoanStatus.CANCELLED);
              }
            });
  }
}
