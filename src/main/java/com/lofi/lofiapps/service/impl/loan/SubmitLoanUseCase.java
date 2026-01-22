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
public class SubmitLoanUseCase {
  private final LoanRepository loanRepository;
  private final ApprovalHistoryRepository approvalHistoryRepository;
  private final NotificationService notificationService;
  private final LoanDtoMapper loanDtoMapper;
  private final com.lofi.lofiapps.repository.JpaDocumentRepository documentRepository;

  private void validateDocuments(UUID loanId) {
    long ktpCount =
        documentRepository.countByLoanIdAndDocumentType(
            loanId, com.lofi.lofiapps.model.enums.DocumentType.KTP);
    long kkCount =
        documentRepository.countByLoanIdAndDocumentType(
            loanId, com.lofi.lofiapps.model.enums.DocumentType.KK);
    long npwpCount =
        documentRepository.countByLoanIdAndDocumentType(
            loanId, com.lofi.lofiapps.model.enums.DocumentType.NPWP);

    if (ktpCount == 0 || kkCount == 0 || npwpCount == 0) {
      throw new IllegalStateException(
          "Required documents missing. Please upload KTP, KK, and NPWP.");
    }
  }

  @Transactional
  public LoanResponse execute(UUID loanId, UUID customerId) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId.toString()));

    if (!loan.getCustomer().getId().equals(customerId)) {
      throw new IllegalArgumentException("Loan does not belong to this customer");
    }

    if (loan.getLoanStatus() != LoanStatus.DRAFT) {
      throw new IllegalStateException("Only draft loans can be submitted");
    }

    // Validate Required Documents
    validateDocuments(loanId);

    LoanStatus fromStatus = loan.getLoanStatus();
    loan.setLoanStatus(LoanStatus.SUBMITTED);
    loan.setCurrentStage(ApprovalStage.MARKETING);
    loan.setSubmittedAt(LocalDateTime.now());
    loan.setLastStatusChangedAt(LocalDateTime.now());

    Loan savedLoan = loanRepository.save(loan);

    // Save history
    approvalHistoryRepository.save(
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(fromStatus)
            .toStatus(LoanStatus.SUBMITTED)
            .actionBy(loan.getCustomer().getUsername())
            .notes("Loan submitted by customer")
            .build());

    // Notify
    notificationService.notifyLoanStatusChange(loan.getCustomer().getId(), LoanStatus.SUBMITTED);

    return loanDtoMapper.toResponse(savedLoan);
  }
}
