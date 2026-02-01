package com.lofi.lofiapps.service.impl.usecase.loan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.entity.Product;
import com.lofi.lofiapps.entity.User;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubmitLoanUseCaseTest {

  @Mock private LoanRepository loanRepository;
  @Mock private DocumentRepository documentRepository;
  @Mock private NotificationService notificationService;
  @Mock private LoanDtoMapper loanDtoMapper;
  @Mock private UserBiodataValidator userBiodataValidator;
  @Mock private RiskValidator riskValidator;
  @Mock private ApprovalHistoryFactory approvalHistoryFactory;

  @InjectMocks private SubmitLoanUseCase submitLoanUseCase;

  private UUID loanId;
  private UUID userId;
  private String username;
  private Loan draftLoan;
  private User customer;
  private UserBiodata userBiodata;
  private LoanResponse expectedResponse;

  @BeforeEach
  void setUp() {
    loanId = UUID.randomUUID();
    userId = UUID.randomUUID();
    username = "testuser";

    customer =
        User.builder()
            .id(userId)
            .username(username)
            .email("test@example.com")
            .profileCompleted(true)
            .product(null)
            .build();

    draftLoan =
        Loan.builder()
            .id(loanId)
            .customer(customer)
            .loanAmount(BigDecimal.valueOf(5000000))
            .tenor(12)
            .loanStatus(LoanStatus.DRAFT)
            .currentStage(ApprovalStage.CUSTOMER)
            .build();

    userBiodata =
        UserBiodata.builder()
            .id(UUID.randomUUID())
            .user(customer)
            .nik("1234567890123456")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .build();

    expectedResponse =
        LoanResponse.builder()
            .id(loanId)
            .loanAmount(BigDecimal.valueOf(5000000))
            .tenor(12)
            .loanStatus(LoanStatus.SUBMITTED)
            .build();
  }

  @Test
  @DisplayName("Execute should submit loan successfully with all requirements met")
  void execute_ShouldSubmitLoanSuccessfully() {
    // Arrange
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(draftLoan));
    when(userBiodataValidator.validateAndGet(userId)).thenReturn(userBiodata);
    when(documentRepository.countByLoanIdAndDocumentType(loanId, DocumentType.KTP)).thenReturn(1L);
    when(documentRepository.countByLoanIdAndDocumentType(loanId, DocumentType.KK)).thenReturn(1L);
    when(documentRepository.countByLoanIdAndDocumentType(loanId, DocumentType.NPWP)).thenReturn(1L);
    doNothing().when(riskValidator).validate(any(User.class), any(UserBiodata.class), any());

    Loan savedLoan =
        Loan.builder()
            .id(loanId)
            .customer(customer)
            .loanAmount(BigDecimal.valueOf(5000000))
            .tenor(12)
            .loanStatus(LoanStatus.SUBMITTED)
            .currentStage(ApprovalStage.MARKETING)
            .submittedAt(LocalDateTime.now())
            .build();

    when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);
    when(loanDtoMapper.toResponse(any(Loan.class))).thenReturn(expectedResponse);
    when(approvalHistoryFactory.recordStatusChange(any(UUID.class), any(), any(), any(), any()))
        .thenReturn(null);
    doNothing().when(notificationService).notifyLoanStatusChange(any(), any());

    // Act
    LoanResponse result = submitLoanUseCase.execute(loanId, username);

    // Assert
    assertNotNull(result);
    assertEquals(loanId, result.getId());
    verify(loanRepository).save(any(Loan.class));
    verify(approvalHistoryFactory)
        .recordStatusChange(
            loanId, LoanStatus.DRAFT, LoanStatus.SUBMITTED, username, "Loan submitted by customer");
    verify(notificationService).notifyLoanStatusChange(userId, LoanStatus.SUBMITTED);
  }

  @Test
  @DisplayName("Execute should throw exception when loan not found")
  void execute_ShouldThrowException_WhenLoanNotFound() {
    // Arrange
    when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class, () -> submitLoanUseCase.execute(loanId, username));
    assertEquals("Loan not found with id : '" + loanId + "'", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when user profile is incomplete")
  void execute_ShouldThrowException_WhenProfileIncomplete() {
    // Arrange
    customer.setProfileCompleted(false);
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(draftLoan));

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> submitLoanUseCase.execute(loanId, username));
    assertEquals(
        "User profile is incomplete. Please complete your profile first.", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when loan is not in DRAFT status")
  void execute_ShouldThrowException_WhenLoanNotDraft() {
    // Arrange
    draftLoan.setLoanStatus(LoanStatus.SUBMITTED);
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(draftLoan));

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> submitLoanUseCase.execute(loanId, username));
    assertEquals("Only draft loans can be submitted", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when user already has assigned product")
  void execute_ShouldThrowException_WhenUserHasAssignedProduct() {
    // Arrange
    Product assignedProduct =
        Product.builder().id(UUID.randomUUID()).productName("Test Product").build();
    customer.setProduct(assignedProduct);
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(draftLoan));

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> submitLoanUseCase.execute(loanId, username));
    assertEquals(
        "User already has an assigned product. Cannot submit this loan.", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when KTP document is missing")
  void execute_ShouldThrowException_WhenKtpMissing() {
    // Arrange
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(draftLoan));
    when(userBiodataValidator.validateAndGet(userId)).thenReturn(userBiodata);
    when(documentRepository.countByLoanIdAndDocumentType(loanId, DocumentType.KTP)).thenReturn(0L);
    when(documentRepository.countByLoanIdAndDocumentType(loanId, DocumentType.KK)).thenReturn(1L);
    when(documentRepository.countByLoanIdAndDocumentType(loanId, DocumentType.NPWP)).thenReturn(1L);

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> submitLoanUseCase.execute(loanId, username));
    assertEquals(
        "Required documents missing. Please upload KTP, KK, and NPWP.", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when KK document is missing")
  void execute_ShouldThrowException_WhenKkMissing() {
    // Arrange
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(draftLoan));
    when(userBiodataValidator.validateAndGet(userId)).thenReturn(userBiodata);
    when(documentRepository.countByLoanIdAndDocumentType(loanId, DocumentType.KTP)).thenReturn(1L);
    when(documentRepository.countByLoanIdAndDocumentType(loanId, DocumentType.KK)).thenReturn(0L);
    when(documentRepository.countByLoanIdAndDocumentType(loanId, DocumentType.NPWP)).thenReturn(1L);

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> submitLoanUseCase.execute(loanId, username));
    assertEquals(
        "Required documents missing. Please upload KTP, KK, and NPWP.", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when NPWP document is missing")
  void execute_ShouldThrowException_WhenNpwpMissing() {
    // Arrange
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(draftLoan));
    when(userBiodataValidator.validateAndGet(userId)).thenReturn(userBiodata);
    when(documentRepository.countByLoanIdAndDocumentType(loanId, DocumentType.KTP)).thenReturn(1L);
    when(documentRepository.countByLoanIdAndDocumentType(loanId, DocumentType.KK)).thenReturn(1L);
    when(documentRepository.countByLoanIdAndDocumentType(loanId, DocumentType.NPWP)).thenReturn(0L);

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> submitLoanUseCase.execute(loanId, username));
    assertEquals(
        "Required documents missing. Please upload KTP, KK, and NPWP.", exception.getMessage());
  }
}
