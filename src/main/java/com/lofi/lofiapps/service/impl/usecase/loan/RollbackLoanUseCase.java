package com.lofi.lofiapps.service.impl.usecase.loan;

import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.enums.ApprovalStage;
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
public class RollbackLoanUseCase {

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

    LoanStatus fromStatus = loan.getLoanStatus();
    LoanStatus toStatus;
    ApprovalStage toStage;

    if (fromStatus == LoanStatus.REVIEWED) {
      // Marketing rollbacks REVIEWED to SUBMITTED
      toStatus = LoanStatus.SUBMITTED;
      toStage = ApprovalStage.MARKETING;
    } else if (fromStatus == LoanStatus.APPROVED) {
      // Branch Manager rollbacks APPROVED to REVIEWED
      toStatus = LoanStatus.REVIEWED;
      toStage = ApprovalStage.BRANCH_MANAGER;
    } else {
      throw new IllegalStateException("Rollback not allowed from status: " + fromStatus);
    }

    loan.setLoanStatus(toStatus);
    loan.setCurrentStage(toStage);
    loan.setLastStatusChangedAt(LocalDateTime.now());

    Loan savedLoan = loanRepository.save(loan);

    // Save history
    // Save history
    approvalHistoryFactory.recordStatusChange(
        loan.getId(), fromStatus, toStatus, officerUsername, notes);

    // Notify customer
    notificationService.notifyLoanStatusChange(loan.getCustomer().getId(), toStatus);

    return loanDtoMapper.toResponse(savedLoan);
  }
}
