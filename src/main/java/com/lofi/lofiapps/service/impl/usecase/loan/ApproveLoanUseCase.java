package com.lofi.lofiapps.service.impl.usecase.loan;

import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.ApprovalStage;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.BranchAccessGuard;
import com.lofi.lofiapps.service.LoanActionValidator;
import com.lofi.lofiapps.service.NotificationService;
import com.lofi.lofiapps.service.RoleActionGuard;
import com.lofi.lofiapps.service.impl.calculator.PlafondCalculator;
import com.lofi.lofiapps.service.impl.factory.ApprovalHistoryFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApproveLoanUseCase {

  private final LoanRepository loanRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final RoleActionGuard roleActionGuard;
  private final BranchAccessGuard branchAccessGuard;
  private final LoanActionValidator loanActionValidator;
  private final LoanDtoMapper loanDtoMapper;
  private final PlafondCalculator plafondCalculator;
  private final ApprovalHistoryFactory approvalHistoryFactory;

  @Transactional
  public LoanResponse execute(UUID loanId, String approverUsername, String notes) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId.toString()));

    User user =
        userRepository
            .findByEmail(approverUsername)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", approverUsername));

    // Guards
    roleActionGuard.validate(user, "approve");
    branchAccessGuard.validate(user, loan);
    loanActionValidator.validate(loan, "approve");

    // Check if customer already has an APPROVED or DISBURSED or COMPLETED loan
    if (loan.getCustomer() == null) {
      throw new IllegalStateException(
          "Loan (ID: " + loanId + ") does not have a customer assigned.");
    }
    UUID customerId = loan.getCustomer().getId();
    User customer = loan.getCustomer();

    // Validate available plafond before approving
    if (customer.getProduct() != null) {
      BigDecimal availablePlafond = plafondCalculator.calculateAvailablePlafond(customer, loanId);
      if (loan.getLoanAmount().compareTo(availablePlafond) > 0) {
        throw new IllegalStateException(
            "Cannot approve loan: Loan amount ("
                + loan.getLoanAmount()
                + ") exceeds available plafond ("
                + availablePlafond
                + ")");
      }
    }

    boolean hasApprovedLoan =
        loanRepository.findByCustomerId(customerId).stream()
            .filter(l -> !l.getId().equals(loanId)) // Exclude current loan
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
    // Save history
    approvalHistoryFactory.recordStatusChange(
        loan.getId(), fromStatus, LoanStatus.APPROVED, approverUsername, notes);

    // Auto-cancel other active loans for this customer
    cancelOtherActiveLoans(customerId, loan.getId());

    // Notify customer
    notificationService.notifyLoanStatusChange(customerId, LoanStatus.APPROVED);

    return loanDtoMapper.toResponse(savedLoan);
  }

  private void cancelOtherActiveLoans(UUID customerId, UUID approvedLoanId) {
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

              approvalHistoryFactory.recordStatusChange(
                  l.getId(),
                  oldStatus,
                  LoanStatus.CANCELLED,
                  "SYSTEM",
                  "Auto-cancelled because another loan was approved");

              if (l.getCustomer() != null) {
                notificationService.notifyLoanStatusChange(
                    l.getCustomer().getId(), LoanStatus.CANCELLED);
              }
            });
  }
}
