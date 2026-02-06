package com.lofi.lofiapps.service.impl.usecase.loan;

import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.entity.UserBiodata;
import com.lofi.lofiapps.enums.ApprovalStage;
import com.lofi.lofiapps.enums.DocumentType;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.repository.DocumentRepository;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.service.NotificationService;
import com.lofi.lofiapps.service.impl.factory.ApprovalHistoryFactory;
import com.lofi.lofiapps.service.impl.validator.RiskValidator;
import com.lofi.lofiapps.service.impl.validator.UserBiodataValidator;
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
  private final NotificationService notificationService;
  private final LoanDtoMapper loanDtoMapper;
  private final UserBiodataValidator userBiodataValidator;
  private final RiskValidator riskValidator;
  private final ApprovalHistoryFactory approvalHistoryFactory;

  @Transactional
  public LoanResponse execute(UUID loanId, String username) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId.toString()));

    if (!loan.getCustomer().getUsername().equals(username)) {
      log.warn(
          "User {} is submitting loan {} which belongs to {}",
          username,
          loanId,
          loan.getCustomer().getUsername());
    }

    if (!Boolean.TRUE.equals(loan.getCustomer().getProfileCompleted())) {
      throw new IllegalStateException(
          "User profile is incomplete. Please complete your profile first.");
    }

    // Check if user already has a product assigned
    // DISABLED: ApplyLoanUseCase requires a valid product assignment, so this check
    // blocks valid submissions.
    if (loan.getLoanStatus() != LoanStatus.DRAFT) {
      throw new IllegalStateException("Only draft loans can be submitted");
    }

    /*
     * if (loan.getCustomer().getProduct() != null) {
     * throw new IllegalStateException(
     * "User already has an assigned product. Cannot submit this loan.");
     * }
     */

    // Check if user biodata is complete using validator
    UserBiodata userBiodata = userBiodataValidator.validateAndGet(loan.getCustomer().getId());

    validateDocuments(loanId);

    // Risk Condition Check using validator
    riskValidator.validate(loan.getCustomer(), userBiodata, loan.getLoanAmount());

    LoanStatus fromStatus = loan.getLoanStatus();
    loan.setLoanStatus(LoanStatus.SUBMITTED);
    loan.setCurrentStage(ApprovalStage.MARKETING);
    loan.setSubmittedAt(LocalDateTime.now());
    loan.setLastStatusChangedAt(LocalDateTime.now());

    Loan savedLoan = loanRepository.save(loan);

    approvalHistoryFactory.recordStatusChange(
        savedLoan.getId(),
        fromStatus,
        LoanStatus.SUBMITTED,
        username,
        "Loan submitted by customer");

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
