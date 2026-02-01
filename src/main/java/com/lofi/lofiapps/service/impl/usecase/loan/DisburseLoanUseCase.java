package com.lofi.lofiapps.service.impl.usecase.loan;

import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.ApprovalHistory;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.repository.ApprovalHistoryRepository;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.service.NotificationService;
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
  private final ApprovalHistoryRepository approvalHistoryRepository;
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
    approvalHistoryRepository.save(
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(fromStatus)
            .toStatus(LoanStatus.DISBURSED)
            .actionBy(officerUsername)
            .notes("Loan disbursed with reference: " + notes)
            .build());

    // Notify customer
    try {
      if (loan.getCustomer() != null && loan.getCustomer().getId() != null) {
        notificationService.notifyLoanStatusChange(
            loan.getCustomer().getId(), LoanStatus.DISBURSED);
      } else {
        log.warn("Loan {} disbursed but no customer associated or customer ID is null", loanId);
      }
    } catch (Exception e) {
      log.error("Failed to notify customer for loan disbursement {}: {}", loanId, e.getMessage());
    }

    return loanDtoMapper.toResponse(savedLoan);
  }
}
