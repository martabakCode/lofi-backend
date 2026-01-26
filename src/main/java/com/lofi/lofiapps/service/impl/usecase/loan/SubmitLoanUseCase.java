package com.lofi.lofiapps.service.impl.usecase.loan;

import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.ApprovalHistory;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.enums.ApprovalStage;
import com.lofi.lofiapps.enums.DocumentType;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.repository.ApprovalHistoryRepository;
import com.lofi.lofiapps.repository.DocumentRepository;
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
public class SubmitLoanUseCase {

  private final LoanRepository loanRepository;
  private final DocumentRepository documentRepository;
  private final ApprovalHistoryRepository approvalHistoryRepository;
  private final NotificationService notificationService;
  private final LoanDtoMapper loanDtoMapper;

  @Transactional
  public LoanResponse execute(UUID loanId, String username) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId.toString()));

    // Verify ownership?
    // Note: The controller/service didn't consistently check ownership other than
    // relying on guards or initial query.
    // The previous implementation had: if
    // (!loan.getCustomer().getUsername().equals(username)) { // Log warning }
    if (!loan.getCustomer().getUsername().equals(username)) {
      // Log warning or throw if strict
      log.warn(
          "User {} is submitting loan {} which belongs to {}",
          username,
          loanId,
          loan.getCustomer().getUsername());
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
            .actionBy(username)
            .notes("Loan submitted by customer")
            .build());

    // Notify
    notificationService.notifyLoanStatusChange(loan.getCustomer().getId(), LoanStatus.SUBMITTED);

    return loanDtoMapper.toResponse(savedLoan);
  }

  private void validateDocuments(UUID loanId) {
    long ktpCount = documentRepository.countByLoanIdAndDocumentType(loanId, DocumentType.KTP);
    long kkCount = documentRepository.countByLoanIdAndDocumentType(loanId, DocumentType.KK);
    long npwpCount = documentRepository.countByLoanIdAndDocumentType(loanId, DocumentType.NPWP);

    if (ktpCount == 0 || kkCount == 0 || npwpCount == 0) {
      throw new IllegalStateException(
          "Required documents missing. Please upload KTP, KK, and NPWP.");
    }
  }
}
