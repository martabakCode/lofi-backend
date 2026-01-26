package com.lofi.lofiapps.usecase.loan;

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
public class CancelLoanUseCase {

    private final LoanRepository loanRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final NotificationService notificationService;
    private final LoanDtoMapper loanDtoMapper;

    @Transactional
    public LoanResponse execute(UUID loanId, String cancellerUsername, String reason) {
        Loan loan = loanRepository
                .findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId.toString()));

        LoanStatus fromStatus = loan.getLoanStatus();
        LoanStatus toStatus = LoanStatus.CANCELLED;

        loan.setLoanStatus(toStatus);
        // Note: service didn't set rejectedAt for cancel, just status and
        // lastStatusChangedAt.
        // wait, service code: `loan.setRejectedAt(LocalDateTime.now());` was called for
        // BOTH Reject and Cancel in `executeRejectOrCancel`.
        // I should check if `rejectedAt` is appropriate for Cancel. Probably yes, as it
        // terminates the loan.
        // I will include it to match original behavior.
        loan.setRejectedAt(LocalDateTime.now());
        loan.setLastStatusChangedAt(LocalDateTime.now());

        Loan savedLoan = loanRepository.save(loan);

        // Save history
        approvalHistoryRepository.save(
                ApprovalHistory.builder()
                        .loanId(loan.getId())
                        .fromStatus(fromStatus)
                        .toStatus(toStatus)
                        .actionBy(cancellerUsername)
                        .notes(reason)
                        .build());

        // Notify customer
        notificationService.notifyLoanStatusChange(loan.getCustomer().getId(), toStatus);

        return loanDtoMapper.toResponse(savedLoan);
    }
}
