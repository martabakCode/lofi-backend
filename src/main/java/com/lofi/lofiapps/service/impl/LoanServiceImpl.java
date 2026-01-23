package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.model.dto.request.LoanCriteria;
import com.lofi.lofiapps.model.dto.request.LoanRequest;
import com.lofi.lofiapps.model.dto.response.BackOfficeRiskEvaluationResponse;
import com.lofi.lofiapps.model.dto.response.BranchManagerSupportResponse;
import com.lofi.lofiapps.model.dto.response.DocumentResponse;
import com.lofi.lofiapps.model.dto.response.LoanAnalysisResponse;
import com.lofi.lofiapps.model.dto.response.LoanResponse;
import com.lofi.lofiapps.model.dto.response.MarketingLoanReviewResponse;
import com.lofi.lofiapps.model.dto.response.PagedResponse;
import com.lofi.lofiapps.model.dto.response.ProductResponse;
import com.lofi.lofiapps.model.entity.ApprovalHistory;
import com.lofi.lofiapps.model.entity.Loan;
import com.lofi.lofiapps.model.entity.Product;
import com.lofi.lofiapps.model.entity.User;
import com.lofi.lofiapps.model.enums.ApprovalStage;
import com.lofi.lofiapps.model.enums.LoanStatus;
import com.lofi.lofiapps.model.enums.UserStatus;
import com.lofi.lofiapps.repository.ApprovalHistoryRepository;
import com.lofi.lofiapps.repository.JpaDocumentRepository;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.repository.ProductRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.BranchAccessGuard;
import com.lofi.lofiapps.service.LoanActionValidator;
import com.lofi.lofiapps.service.LoanService;
import com.lofi.lofiapps.service.NotificationService;
import com.lofi.lofiapps.service.RoleActionGuard;
import com.lofi.lofiapps.service.impl.loan.AnalyzeLoanUseCase;
import com.lofi.lofiapps.service.impl.loan.BackOfficeRiskEvaluationUseCase;
import com.lofi.lofiapps.service.impl.loan.BranchManagerSupportUseCase;
import com.lofi.lofiapps.service.impl.loan.MarketingReviewLoanUseCase;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class LoanServiceImpl implements LoanService {

  private final LoanRepository loanRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final ApprovalHistoryRepository approvalHistoryRepository;
  private final JpaDocumentRepository documentRepository;
  private final LoanDtoMapper loanDtoMapper;

  private final NotificationService notificationService;

  private final RoleActionGuard roleActionGuard;
  private final BranchAccessGuard branchAccessGuard;
  private final LoanActionValidator loanActionValidator;

  private final AnalyzeLoanUseCase analyzeLoanUseCase;
  private final MarketingReviewLoanUseCase marketingReviewLoanUseCase;
  private final BackOfficeRiskEvaluationUseCase backOfficeRiskEvaluationUseCase;
  private final BranchManagerSupportUseCase branchManagerSupportUseCase;

  public LoanServiceImpl(
      LoanRepository loanRepository,
      UserRepository userRepository,
      ProductRepository productRepository,
      ApprovalHistoryRepository approvalHistoryRepository,
      JpaDocumentRepository documentRepository,
      LoanDtoMapper loanDtoMapper,
      NotificationService notificationService,
      RoleActionGuard roleActionGuard,
      BranchAccessGuard branchAccessGuard,
      LoanActionValidator loanActionValidator,
      AnalyzeLoanUseCase analyzeLoanUseCase,
      MarketingReviewLoanUseCase marketingReviewLoanUseCase,
      BackOfficeRiskEvaluationUseCase backOfficeRiskEvaluationUseCase,
      BranchManagerSupportUseCase branchManagerSupportUseCase) {
    this.loanRepository = loanRepository;
    this.userRepository = userRepository;
    this.productRepository = productRepository;
    this.approvalHistoryRepository = approvalHistoryRepository;
    this.documentRepository = documentRepository;
    this.loanDtoMapper = loanDtoMapper;
    this.notificationService = notificationService;
    this.roleActionGuard = roleActionGuard;
    this.branchAccessGuard = branchAccessGuard;
    this.loanActionValidator = loanActionValidator;
    this.analyzeLoanUseCase = analyzeLoanUseCase;
    this.marketingReviewLoanUseCase = marketingReviewLoanUseCase;
    this.backOfficeRiskEvaluationUseCase = backOfficeRiskEvaluationUseCase;
    this.branchManagerSupportUseCase = branchManagerSupportUseCase;
  }

  @Override
  @Transactional
  public LoanResponse applyLoan(LoanRequest request, UUID userId, String username) {
    if (request.getLoanAmount() == null || request.getTenor() == null) {
      throw new IllegalArgumentException("Loan amount and tenor are required");
    }

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new IllegalStateException("User is not active");
    }

    if (!Boolean.TRUE.equals(user.getProfileCompleted())) {
      throw new IllegalStateException(
          "User profile is incomplete. Please complete your profile first.");
    }

    Product product =
        productRepository
            .findById(request.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

    // Validation
    if (request.getLoanAmount().compareTo(product.getMinLoanAmount()) < 0) {
      throw new IllegalArgumentException(
          "Loan amount is less than minimum: " + product.getMinLoanAmount());
    }
    if (request.getLoanAmount().compareTo(product.getMaxLoanAmount()) > 0) {
      throw new IllegalArgumentException(
          "Loan amount exceeds maximum: " + product.getMaxLoanAmount());
    }
    if (request.getTenor() > product.getMaxTenor()) {
      throw new IllegalArgumentException("Tenor exceeds maximum: " + product.getMaxTenor());
    }

    // Create Draft Loan
    Loan loan =
        Loan.builder()
            .loanAmount(request.getLoanAmount())
            .tenor(request.getTenor())
            .loanStatus(LoanStatus.DRAFT)
            .currentStage(ApprovalStage.CUSTOMER)
            .customer(user)
            .product(product)
            .branch(user.getBranch())
            .submittedAt(null) // Not submitted yet
            .build();

    Loan savedLoan = loanRepository.save(loan);

    // Save history for DRAFT creation (Tracking)
    approvalHistoryRepository.save(
        ApprovalHistory.builder()
            .loanId(savedLoan.getId())
            .fromStatus(null)
            .toStatus(LoanStatus.DRAFT)
            .actionBy(username)
            .notes("Loan Draft Created")
            .build());

    return loanDtoMapper.toResponse(savedLoan);
  }

  @Override
  public PagedResponse<LoanResponse> getLoans(LoanCriteria criteria, Pageable pageable) {
    Page<Loan> page = loanRepository.findAll(criteria, pageable);

    List<LoanResponse> items =
        page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());

    return PagedResponse.of(
        items, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
  }

  @Override
  public PagedResponse<LoanResponse> getMyLoans(UUID customerId, Pageable pageable) {
    // Return all loans for this customer
    LoanCriteria criteria = LoanCriteria.builder().customerId(customerId).build();
    return getLoans(criteria, pageable);
  }

  @Override
  public PagedResponse<LoanResponse> getLoanHistory(UUID customerId, Pageable pageable) {
    // History typically means finished loans (COMPLETED, REJECTED, CANCELLED,
    // DISBURSED?)
    // For now, let's return all loans, but could be filtered.
    // Or maybe filter by != DRAFT && != SUBMITTED && != REVIEWED && != APPROVED?
    // "Custom Loan History" could imply a specific view.
    // I will implement it as getting ALL loans for now, similar to getMyLoans,
    // unless specific status requested.
    // If the requirement implies "History" vs "Active", then:
    // Active: SUBMITTED, REVIEWED, APPROVED, DISBURSED
    // History: COMPLETED, REJECTED, CANCELLED
    // But user just said "custom loan history". I'll return all relevant loans for
    // the user.
    LoanCriteria criteria = LoanCriteria.builder().customerId(customerId).build();
    return getLoans(criteria, pageable);
  }

  private LoanResponse mapToResponse(Loan loan) {
    return LoanResponse.builder()
        .id(loan.getId())
        .customerId(loan.getCustomer() != null ? loan.getCustomer().getId() : null)
        .customerName(loan.getCustomer() != null ? loan.getCustomer().getFullName() : null)
        .product(
            loan.getProduct() != null
                ? ProductResponse.builder()
                    .id(loan.getProduct().getId())
                    .productCode(loan.getProduct().getProductCode())
                    .productName(loan.getProduct().getProductName())
                    .interestRate(loan.getProduct().getInterestRate())
                    .build()
                : null)
        .loanAmount(loan.getLoanAmount())
        .tenor(loan.getTenor())
        .loanStatus(loan.getLoanStatus())
        .currentStage(loan.getCurrentStage())
        .submittedAt(loan.getSubmittedAt())
        .approvedAt(loan.getApprovedAt())
        .rejectedAt(loan.getRejectedAt())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public LoanResponse getLoanDetail(UUID loanId) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
    LoanResponse response = loanDtoMapper.toResponse(loan);

    // Fetch documents
    List<DocumentResponse> documents =
        documentRepository.findByLoanId(loanId).stream()
            .map(
                doc ->
                    DocumentResponse.builder()
                        .id(doc.getId())
                        .fileName(doc.getFileName())
                        .documentType(doc.getDocumentType())
                        .uploadedAt(doc.getCreatedAt())
                        .build())
            .collect(Collectors.toList());

    response.setDocuments(documents);
    return response;
  }

  @Override
  @Transactional
  public LoanResponse approveLoan(UUID loanId, String approverUsername, String notes) {
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
    boolean hasApprovedLoan =
        loanRepository.findByCustomerId(customerId).stream()
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
    approvalHistoryRepository.save(
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(fromStatus)
            .toStatus(LoanStatus.APPROVED)
            .actionBy(approverUsername)
            .notes(notes)
            .build());

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

              approvalHistoryRepository.save(
                  ApprovalHistory.builder()
                      .loanId(l.getId())
                      .fromStatus(oldStatus)
                      .toStatus(LoanStatus.CANCELLED)
                      .actionBy("SYSTEM")
                      .notes("Auto-cancelled because another loan was approved")
                      .build());

              if (l.getCustomer() != null) {
                notificationService.notifyLoanStatusChange(
                    l.getCustomer().getId(), LoanStatus.CANCELLED);
              }
            });
  }

  @Override
  @Transactional
  public LoanResponse rejectLoan(UUID loanId, String rejectorUsername, String notes) {
    return executeRejectOrCancel(loanId, rejectorUsername, notes, false);
  }

  @Override
  @Transactional
  public LoanResponse cancelLoan(UUID loanId, String cancellerUsername, String reason) {
    return executeRejectOrCancel(loanId, cancellerUsername, reason, true);
  }

  private LoanResponse executeRejectOrCancel(
      UUID loanId, String actionBy, String notes, boolean isCancel) {
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

  @Override
  @Transactional
  public LoanResponse disburseLoan(UUID loanId, String officerUsername, String notes) {
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

  @Override
  @Transactional
  public LoanResponse reviewLoan(UUID loanId, String reviewerUsername, String notes) {
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

  @Override
  @Transactional
  public LoanResponse rollbackLoan(UUID loanId, String officerUsername, String notes) {
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
    approvalHistoryRepository.save(
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(fromStatus)
            .toStatus(toStatus)
            .actionBy(officerUsername)
            .notes(notes)
            .build());

    // Notify customer
    notificationService.notifyLoanStatusChange(loan.getCustomer().getId(), toStatus);

    return loanDtoMapper.toResponse(savedLoan);
  }

  @Override
  @Transactional
  public LoanResponse submitLoan(UUID loanId, String username) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId.toString()));

    // Verify ownership?
    if (!loan.getCustomer().getUsername().equals(username)) {
      // Log warning or throw if strict
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

  @Override
  @Transactional
  public LoanResponse completeLoan(UUID loanId, String username) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId.toString()));

    if (loan.getLoanStatus() != LoanStatus.DISBURSED) {
      throw new IllegalStateException("Only disbursed loans can be completed");
    }

    LoanStatus fromStatus = loan.getLoanStatus();
    loan.setLoanStatus(LoanStatus.COMPLETED);
    loan.setLastStatusChangedAt(LocalDateTime.now());

    Loan savedLoan = loanRepository.save(loan);

    // Save history
    approvalHistoryRepository.save(
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(fromStatus)
            .toStatus(LoanStatus.COMPLETED)
            .actionBy(username)
            .notes("Loan completed")
            .build());

    // Notify
    notificationService.notifyLoanStatusChange(loan.getCustomer().getId(), LoanStatus.COMPLETED);

    return loanDtoMapper.toResponse(savedLoan);
  }

  @Override
  public LoanAnalysisResponse analyzeLoan(UUID loanId) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + loanId));

    return analyzeLoanUseCase.execute(loan);
  }

  @Override
  public MarketingLoanReviewResponse marketingReviewLoan(UUID loanId) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + loanId));
    return marketingReviewLoanUseCase.execute(loan);
  }

  @Override
  public BackOfficeRiskEvaluationResponse analyzeBackOfficeRiskEvaluation(UUID loanId) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + loanId));

    return backOfficeRiskEvaluationUseCase.execute(loan);
  }

  @Override
  public BranchManagerSupportResponse analyzeLoanBranchSupport(UUID loanId) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + loanId));

    return branchManagerSupportUseCase.execute(loan);
  }
}
