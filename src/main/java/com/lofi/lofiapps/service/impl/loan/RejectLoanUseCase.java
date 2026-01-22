package com.lofi.lofiapps.service.impl.loan;

import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.model.dto.response.LoanResponse;
import com.lofi.lofiapps.model.entity.ApprovalHistory;
import com.lofi.lofiapps.model.entity.Loan;
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
public class RejectLoanUseCase {
  private final LoanRepository loanRepository;
  private final ApprovalHistoryRepository approvalHistoryRepository;
  private final NotificationService notificationService;
  private final LoanDtoMapper loanDtoMapper;

  @Transactional
  public LoanResponse execute(UUID loanId, String actionBy, String notes, boolean isCancel) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId.toString()));

    LoanStatus fromStatus = loan.getLoanStatus();
    LoanStatus toStatus = isCancel ? LoanStatus.CANCELLED : LoanStatus.REJECTED;

    loan.setLoanStatus(toStatus);
    loan.setRejectedAt(LocalDateTime.now());
    loan.setLastStatusChangedAt(LocalDateTime.now());

    Loan savedLoan = loanRepository.save(loan);

    // Save history
    approvalHistoryRepository.save(
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(fromStatus)
            .toStatus(toStatus)
            .actionBy(actionBy)
            .notes(notes)
            .build());

    // Notify customer
    notificationService.notifyLoanStatusChange(loan.getCustomer().getId(), toStatus);

    return loanDtoMapper.toResponse(savedLoan);
  }
}
