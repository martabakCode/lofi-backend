package com.lofi.lofiapps.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.request.LoanCriteria;
import com.lofi.lofiapps.dto.request.LoanRequest;
import com.lofi.lofiapps.dto.request.MarketingApplyLoanRequest;
import com.lofi.lofiapps.dto.response.*;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.service.impl.usecase.loan.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

  @Mock private ApplyLoanUseCase applyLoanUseCase;
  @Mock private GetLoansUseCase getLoansUseCase;
  @Mock private GetLoanDetailUseCase getLoanDetailUseCase;
  @Mock private ApproveLoanUseCase approveLoanUseCase;
  @Mock private RejectLoanUseCase rejectLoanUseCase;
  @Mock private CancelLoanUseCase cancelLoanUseCase;
  @Mock private DisburseLoanUseCase disburseLoanUseCase;
  @Mock private ReviewLoanUseCase reviewLoanUseCase;
  @Mock private RollbackLoanUseCase rollbackLoanUseCase;
  @Mock private SubmitLoanUseCase submitLoanUseCase;
  @Mock private CompleteLoanUseCase completeLoanUseCase;
  @Mock private MarketingApplyLoanUseCase marketingApplyLoanUseCase;
  @Mock private AnalyzeLoanUseCase analyzeLoanUseCase;
  @Mock private MarketingReviewLoanUseCase marketingReviewLoanUseCase;
  @Mock private BackOfficeRiskEvaluationUseCase backOfficeRiskEvaluationUseCase;
  @Mock private BranchManagerSupportUseCase branchManagerSupportUseCase;

  @InjectMocks private LoanServiceImpl loanService;

  private UUID loanId;
  private UUID userId;
  private String username;
  private LoanResponse loanResponse;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    loanId = UUID.randomUUID();
    userId = UUID.randomUUID();
    username = "testuser";
    pageable = PageRequest.of(0, 10);

    loanResponse =
        LoanResponse.builder()
            .id(loanId)
            .loanAmount(BigDecimal.valueOf(5000000))
            .tenor(12)
            .loanStatus(LoanStatus.DRAFT)
            .build();
  }

  @Test
  @DisplayName("ApplyLoan should delegate to ApplyLoanUseCase")
  void applyLoan_ShouldDelegateToUseCase() {
    // Arrange
    LoanRequest request = new LoanRequest();
    request.setLoanAmount(BigDecimal.valueOf(5000000));
    request.setTenor(12);

    when(applyLoanUseCase.execute(any(LoanRequest.class), any(UUID.class), anyString()))
        .thenReturn(loanResponse);

    // Act
    LoanResponse result = loanService.applyLoan(request, userId, username);

    // Assert
    assertNotNull(result);
    assertEquals(loanId, result.getId());
    verify(applyLoanUseCase).execute(request, userId, username);
  }

  @Test
  @DisplayName("MarketingApplyLoan should delegate to MarketingApplyLoanUseCase")
  void marketingApplyLoan_ShouldDelegateToUseCase() {
    // Arrange
    MarketingApplyLoanRequest request = new MarketingApplyLoanRequest();
    request.setEmail("customer@example.com");
    request.setLoanAmount(BigDecimal.valueOf(5000000));
    request.setTenor(12);

    when(marketingApplyLoanUseCase.execute(any(MarketingApplyLoanRequest.class), anyString()))
        .thenReturn(loanResponse);

    // Act
    LoanResponse result = loanService.marketingApplyLoan(request, username);

    // Assert
    assertNotNull(result);
    verify(marketingApplyLoanUseCase).execute(request, username);
  }

  @Test
  @DisplayName("GetLoans should delegate to GetLoansUseCase")
  void getLoans_ShouldDelegateToUseCase() {
    // Arrange
    LoanCriteria criteria =
        LoanCriteria.builder().status(LoanStatus.DRAFT).customerId(userId).build();
    PagedResponse<LoanResponse> expectedResponse =
        PagedResponse.of(List.of(loanResponse), 0, 10, 1, 1);

    when(getLoansUseCase.execute(any(LoanCriteria.class), any(Pageable.class)))
        .thenReturn(expectedResponse);

    // Act
    PagedResponse<LoanResponse> result = loanService.getLoans(criteria, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getItems().size());
    verify(getLoansUseCase).execute(criteria, pageable);
  }

  @Test
  @DisplayName("GetMyLoans should delegate to GetLoansUseCase with customer criteria")
  void getMyLoans_ShouldDelegateToUseCase() {
    // Arrange
    PagedResponse<LoanResponse> expectedResponse =
        PagedResponse.of(List.of(loanResponse), 0, 10, 1, 1);

    when(getLoansUseCase.execute(any(LoanCriteria.class), any(Pageable.class)))
        .thenReturn(expectedResponse);

    // Act
    PagedResponse<LoanResponse> result = loanService.getMyLoans(userId, pageable);

    // Assert
    assertNotNull(result);
    verify(getLoansUseCase).execute(argThat(c -> c.getCustomerId().equals(userId)), eq(pageable));
  }

  @Test
  @DisplayName("GetLoanHistory should delegate to GetLoansUseCase")
  void getLoanHistory_ShouldDelegateToUseCase() {
    // Arrange
    PagedResponse<LoanResponse> expectedResponse =
        PagedResponse.of(List.of(loanResponse), 0, 10, 1, 1);

    when(getLoansUseCase.execute(any(LoanCriteria.class), any(Pageable.class)))
        .thenReturn(expectedResponse);

    // Act
    PagedResponse<LoanResponse> result = loanService.getLoanHistory(userId, pageable);

    // Assert
    assertNotNull(result);
    verify(getLoansUseCase).execute(argThat(c -> c.getCustomerId().equals(userId)), eq(pageable));
  }

  @Test
  @DisplayName("GetLoanDetail should delegate to GetLoanDetailUseCase")
  void getLoanDetail_ShouldDelegateToUseCase() {
    // Arrange
    when(getLoanDetailUseCase.execute(loanId)).thenReturn(loanResponse);

    // Act
    LoanResponse result = loanService.getLoanDetail(loanId);

    // Assert
    assertNotNull(result);
    assertEquals(loanId, result.getId());
    verify(getLoanDetailUseCase).execute(loanId);
  }

  @Test
  @DisplayName("ApproveLoan should delegate to ApproveLoanUseCase")
  void approveLoan_ShouldDelegateToUseCase() {
    // Arrange
    LoanResponse approvedResponse =
        LoanResponse.builder()
            .id(loanId)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.APPROVED)
            .build();

    when(approveLoanUseCase.execute(any(UUID.class), anyString(), anyString()))
        .thenReturn(approvedResponse);

    // Act
    LoanResponse result = loanService.approveLoan(loanId, username, "Approved");

    // Assert
    assertNotNull(result);
    assertEquals(LoanStatus.APPROVED, result.getLoanStatus());
    verify(approveLoanUseCase).execute(loanId, username, "Approved");
  }

  @Test
  @DisplayName("RejectLoan should delegate to RejectLoanUseCase")
  void rejectLoan_ShouldDelegateToUseCase() {
    // Arrange
    LoanResponse rejectedResponse =
        LoanResponse.builder()
            .id(loanId)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.REJECTED)
            .build();

    when(rejectLoanUseCase.execute(any(UUID.class), anyString(), anyString()))
        .thenReturn(rejectedResponse);

    // Act
    LoanResponse result = loanService.rejectLoan(loanId, username, "Risk too high");

    // Assert
    assertNotNull(result);
    assertEquals(LoanStatus.REJECTED, result.getLoanStatus());
    verify(rejectLoanUseCase).execute(loanId, username, "Risk too high");
  }

  @Test
  @DisplayName("CancelLoan should delegate to CancelLoanUseCase")
  void cancelLoan_ShouldDelegateToUseCase() {
    // Arrange
    LoanResponse cancelledResponse =
        LoanResponse.builder()
            .id(loanId)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.CANCELLED)
            .build();

    when(cancelLoanUseCase.execute(any(UUID.class), anyString(), anyString()))
        .thenReturn(cancelledResponse);

    // Act
    LoanResponse result = loanService.cancelLoan(loanId, username, "Customer request");

    // Assert
    assertNotNull(result);
    assertEquals(LoanStatus.CANCELLED, result.getLoanStatus());
    verify(cancelLoanUseCase).execute(loanId, username, "Customer request");
  }

  @Test
  @DisplayName("DisburseLoan should delegate to DisburseLoanUseCase")
  void disburseLoan_ShouldDelegateToUseCase() {
    // Arrange
    LoanResponse disbursedResponse =
        LoanResponse.builder()
            .id(loanId)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.DISBURSED)
            .build();

    when(disburseLoanUseCase.execute(any(UUID.class), anyString(), anyString()))
        .thenReturn(disbursedResponse);

    // Act
    LoanResponse result = loanService.disburseLoan(loanId, username, "REF123");

    // Assert
    assertNotNull(result);
    assertEquals(LoanStatus.DISBURSED, result.getLoanStatus());
    verify(disburseLoanUseCase).execute(loanId, username, "REF123");
  }

  @Test
  @DisplayName("ReviewLoan should delegate to ReviewLoanUseCase")
  void reviewLoan_ShouldDelegateToUseCase() {
    // Arrange
    LoanResponse reviewedResponse =
        LoanResponse.builder()
            .id(loanId)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.REVIEWED)
            .build();

    when(reviewLoanUseCase.execute(any(UUID.class), anyString(), anyString()))
        .thenReturn(reviewedResponse);

    // Act
    LoanResponse result = loanService.reviewLoan(loanId, username, "Reviewed");

    // Assert
    assertNotNull(result);
    assertEquals(LoanStatus.REVIEWED, result.getLoanStatus());
    verify(reviewLoanUseCase).execute(loanId, username, "Reviewed");
  }

  @Test
  @DisplayName("RollbackLoan should delegate to RollbackLoanUseCase")
  void rollbackLoan_ShouldDelegateToUseCase() {
    // Arrange
    when(rollbackLoanUseCase.execute(any(UUID.class), anyString(), anyString()))
        .thenReturn(loanResponse);

    // Act
    LoanResponse result = loanService.rollbackLoan(loanId, username, "Rollback");

    // Assert
    assertNotNull(result);
    verify(rollbackLoanUseCase).execute(loanId, username, "Rollback");
  }

  @Test
  @DisplayName("SubmitLoan should delegate to SubmitLoanUseCase")
  void submitLoan_ShouldDelegateToUseCase() {
    // Arrange
    LoanResponse submittedResponse =
        LoanResponse.builder()
            .id(loanId)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.SUBMITTED)
            .build();

    when(submitLoanUseCase.execute(any(UUID.class), anyString())).thenReturn(submittedResponse);

    // Act
    LoanResponse result = loanService.submitLoan(loanId, username);

    // Assert
    assertNotNull(result);
    assertEquals(LoanStatus.SUBMITTED, result.getLoanStatus());
    verify(submitLoanUseCase).execute(loanId, username);
  }

  @Test
  @DisplayName("CompleteLoan should delegate to CompleteLoanUseCase")
  void completeLoan_ShouldDelegateToUseCase() {
    // Arrange
    LoanResponse completedResponse =
        LoanResponse.builder()
            .id(loanId)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.COMPLETED)
            .build();

    when(completeLoanUseCase.execute(any(UUID.class), anyString())).thenReturn(completedResponse);

    // Act
    LoanResponse result = loanService.completeLoan(loanId, username);

    // Assert
    assertNotNull(result);
    assertEquals(LoanStatus.COMPLETED, result.getLoanStatus());
    verify(completeLoanUseCase).execute(loanId, username);
  }

  @Test
  @DisplayName("AnalyzeLoan should delegate to AnalyzeLoanUseCase")
  void analyzeLoan_ShouldDelegateToUseCase() {
    // Arrange
    LoanAnalysisResponse analysisResponse =
        LoanAnalysisResponse.builder().confidence(0.85).summary("Good application").build();

    when(analyzeLoanUseCase.execute(loanId)).thenReturn(analysisResponse);

    // Act
    LoanAnalysisResponse result = loanService.analyzeLoan(loanId);

    // Assert
    assertNotNull(result);
    assertEquals(0.85, result.getConfidence());
    verify(analyzeLoanUseCase).execute(loanId);
  }

  @Test
  @DisplayName("MarketingReviewLoan should delegate to MarketingReviewLoanUseCase")
  void marketingReviewLoan_ShouldDelegateToUseCase() {
    // Arrange
    MarketingLoanReviewResponse reviewResponse =
        MarketingLoanReviewResponse.builder().confidence(0.9).notes("Looks good").build();

    when(marketingReviewLoanUseCase.execute(loanId)).thenReturn(reviewResponse);

    // Act
    MarketingLoanReviewResponse result = loanService.marketingReviewLoan(loanId);

    // Assert
    assertNotNull(result);
    assertEquals(0.9, result.getConfidence());
    verify(marketingReviewLoanUseCase).execute(loanId);
  }

  @Test
  @DisplayName("AnalyzeBackOfficeRiskEvaluation should delegate to BackOfficeRiskEvaluationUseCase")
  void analyzeBackOfficeRiskEvaluation_ShouldDelegateToUseCase() {
    // Arrange
    BackOfficeRiskEvaluationResponse riskResponse =
        BackOfficeRiskEvaluationResponse.builder().confidence(0.8).riskOverview("Low risk").build();

    when(backOfficeRiskEvaluationUseCase.execute(loanId)).thenReturn(riskResponse);

    // Act
    BackOfficeRiskEvaluationResponse result = loanService.analyzeBackOfficeRiskEvaluation(loanId);

    // Assert
    assertNotNull(result);
    assertEquals(0.8, result.getConfidence());
    verify(backOfficeRiskEvaluationUseCase).execute(loanId);
  }

  @Test
  @DisplayName("AnalyzeLoanBranchSupport should delegate to BranchManagerSupportUseCase")
  void analyzeLoanBranchSupport_ShouldDelegateToUseCase() {
    // Arrange
    BranchManagerSupportResponse supportResponse =
        BranchManagerSupportResponse.builder()
            .confidence(0.75)
            .attentionPoints(List.of("Check documents"))
            .build();

    when(branchManagerSupportUseCase.execute(loanId)).thenReturn(supportResponse);

    // Act
    BranchManagerSupportResponse result = loanService.analyzeLoanBranchSupport(loanId);

    // Assert
    assertNotNull(result);
    assertEquals(0.75, result.getConfidence());
    verify(branchManagerSupportUseCase).execute(loanId);
  }
}
