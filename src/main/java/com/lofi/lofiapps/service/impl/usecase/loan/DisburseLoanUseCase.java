package com.lofi.lofiapps.service.impl.usecase.loan;

import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.service.NotificationService;
import com.lofi.lofiapps.service.impl.factory.ApprovalHistoryFactory;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisburseLoanUseCase {

  private final LoanRepository loanRepository;
  private final ApprovalHistoryFactory approvalHistoryFactory;
  private final NotificationService notificationService;
  private final LoanDtoMapper loanDtoMapper;

  @Transactional
  public LoanResponse execute(UUID loanId, String officerUsername, String notes) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId.toString()));

    if (loan.getLoanStatus() != LoanStatus.APPROVED) {
      throw new IllegalStateException("Only approved loans can be disbursed");
    }

    LoanStatus fromStatus = loan.getLoanStatus();
    loan.setLoanStatus(LoanStatus.DISBURSED);
    loan.setDisbursedAt(LocalDateTime.now());
    loan.setDisbursementReference(notes); // Using notes as reference
    loan.setLastStatusChangedAt(LocalDateTime.now());

    // Log disbursement account information
    log.info(
        "Disbursing loan {} to account: {} - {} ({})",
        loanId,
        loan.getBankName(),
        loan.getAccountNumber(),
        loan.getAccountHolderName());

    Loan savedLoan = loanRepository.save(loan);

    // Save history
    // Save history
    approvalHistoryFactory.recordStatusChange(
        loan.getId(),
        fromStatus,
        LoanStatus.DISBURSED,
        officerUsername,
        "Loan disbursed with reference: " + notes);

    // Notify customer
    try {
      if (loan.getCustomer() != null) {
        notificationService.notifyLoanDisbursement(savedLoan);
      } else {
        log.warn("Loan {} disbursed but no customer associated", loanId);
      }
    } catch (Exception e) {
      log.error("Failed to notify customer for loan disbursement {}: {}", loanId, e.getMessage());
    }

    return loanDtoMapper.toResponse(savedLoan);
  }
}
