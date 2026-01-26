package com.lofi.lofiapps.service.impl.usecase.loan;

import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.ApprovalHistory;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.enums.ApprovalStage;
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
public class ReviewLoanUseCase {

  private final LoanRepository loanRepository;
  private final ApprovalHistoryRepository approvalHistoryRepository;
  private final NotificationService notificationService;
  private final LoanDtoMapper loanDtoMapper;

  @Transactional
  public LoanResponse execute(UUID loanId, String reviewerUsername, String notes) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId.toString()));

    if (loan.getLoanStatus() != LoanStatus.SUBMITTED) {
      throw new IllegalStateException("Only submitted loans can be reviewed");
    }

    LoanStatus fromStatus = loan.getLoanStatus();
    loan.setLoanStatus(LoanStatus.REVIEWED);
    loan.setCurrentStage(ApprovalStage.BRANCH_MANAGER);
    loan.setLastStatusChangedAt(LocalDateTime.now());

    Loan savedLoan = loanRepository.save(loan);

    // Save history
    approvalHistoryRepository.save(
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(fromStatus)
            .toStatus(LoanStatus.REVIEWED)
            .actionBy(reviewerUsername)
            .notes(notes)
            .build());

    // Notify customer
    notificationService.notifyLoanStatusChange(loan.getCustomer().getId(), LoanStatus.REVIEWED);

    return loanDtoMapper.toResponse(savedLoan);
  }
}
