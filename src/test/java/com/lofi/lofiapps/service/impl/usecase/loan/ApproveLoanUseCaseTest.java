package com.lofi.lofiapps.service.impl.usecase.loan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.entity.Product;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.ApprovalStage;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.repository.ApprovalHistoryRepository;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.BranchAccessGuard;
import com.lofi.lofiapps.service.LoanActionValidator;
import com.lofi.lofiapps.service.NotificationService;
import com.lofi.lofiapps.service.RoleActionGuard;
import java.math.BigDecimal;
import java.util.Collections;
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
class ApproveLoanUseCaseTest {

  @Mock private LoanRepository loanRepository;
  @Mock private UserRepository userRepository;
  @Mock private ApprovalHistoryRepository approvalHistoryRepository;
  @Mock private NotificationService notificationService;
  @Mock private RoleActionGuard roleActionGuard;
  @Mock private BranchAccessGuard branchAccessGuard;
  @Mock private LoanActionValidator loanActionValidator;
  @Mock private LoanDtoMapper loanDtoMapper;

  @InjectMocks private ApproveLoanUseCase approveLoanUseCase;

  private UUID loanId;
  private UUID customerId;
  private String approverUsername;
  private Loan reviewedLoan;
  private User approver;
  private User customer;
  private Product product;
  private LoanResponse expectedResponse;

  @BeforeEach
  void setUp() {
    loanId = UUID.randomUUID();
    customerId = UUID.randomUUID();
    approverUsername = "approver@example.com";

    approver =
        User.builder().id(UUID.randomUUID()).email(approverUsername).username("approver").build();

    product =
        Product.builder()
            .id(UUID.randomUUID())
            .productName("Test Product")
            .maxLoanAmount(BigDecimal.valueOf(10000000))
            .build();

    customer =
        User.builder()
            .id(customerId)
            .email("customer@example.com")
            .username("customer")
            .product(product)
            .build();

    reviewedLoan =
        Loan.builder()
            .id(loanId)
            .customer(customer)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.REVIEWED)
            .currentStage(ApprovalStage.MARKETING)
            .build();

    expectedResponse =
        LoanResponse.builder()
            .id(loanId)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.APPROVED)
            .build();
  }

  @Test
  @DisplayName("Execute should approve loan successfully with all requirements met")
  void execute_ShouldApproveLoanSuccessfully() {
    // Arrange
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(reviewedLoan));
    when(userRepository.findByEmail(approverUsername)).thenReturn(Optional.of(approver));
    doNothing().when(roleActionGuard).validate(any(User.class), eq("approve"));
    doNothing().when(branchAccessGuard).validate(any(User.class), any(Loan.class));
    doNothing().when(loanActionValidator).validate(any(Loan.class), eq("approve"));
    when(loanRepository.findByCustomerId(customerId)).thenReturn(Collections.emptyList());

    Loan savedLoan =
        Loan.builder()
            .id(loanId)
            .customer(customer)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.APPROVED)
            .currentStage(ApprovalStage.BACKOFFICE)
            .build();

    when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);
    when(loanDtoMapper.toResponse(any(Loan.class))).thenReturn(expectedResponse);
    when(approvalHistoryRepository.save(any())).thenReturn(null);
    doNothing().when(notificationService).notifyLoanStatusChange(any(), any());

    // Act
    LoanResponse result = approveLoanUseCase.execute(loanId, approverUsername, "Approved");

    // Assert
    assertNotNull(result);
    assertEquals(LoanStatus.APPROVED, result.getLoanStatus());
    verify(loanRepository).save(any(Loan.class));
    verify(approvalHistoryRepository).save(any());
    verify(notificationService).notifyLoanStatusChange(customerId, LoanStatus.APPROVED);
  }

  @Test
  @DisplayName("Execute should throw exception when loan not found")
  void execute_ShouldThrowException_WhenLoanNotFound() {
    // Arrange
    when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> approveLoanUseCase.execute(loanId, approverUsername, "Approved"));
    assertEquals("Loan", exception.getResourceName());
  }

  @Test
  @DisplayName("Execute should throw exception when approver not found")
  void execute_ShouldThrowException_WhenApproverNotFound() {
    // Arrange
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(reviewedLoan));
    when(userRepository.findByEmail(approverUsername)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> approveLoanUseCase.execute(loanId, approverUsername, "Approved"));
    assertEquals("User", exception.getResourceName());
  }

  @Test
  @DisplayName("Execute should throw exception when loan has no customer assigned")
  void execute_ShouldThrowException_WhenLoanHasNoCustomer() {
    // Arrange
    reviewedLoan.setCustomer(null);
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(reviewedLoan));
    when(userRepository.findByEmail(approverUsername)).thenReturn(Optional.of(approver));
    doNothing().when(roleActionGuard).validate(any(User.class), eq("approve"));
    doNothing().when(branchAccessGuard).validate(any(User.class), any(Loan.class));
    doNothing().when(loanActionValidator).validate(any(Loan.class), eq("approve"));

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> approveLoanUseCase.execute(loanId, approverUsername, "Approved"));
    assertTrue(exception.getMessage().contains("does not have a customer assigned"));
  }

  @Test
  @DisplayName("Execute should throw exception when loan amount exceeds available plafond")
  void execute_ShouldThrowException_WhenLoanAmountExceedsPlafond() {
    // Arrange
    reviewedLoan.setLoanAmount(BigDecimal.valueOf(15000000)); // Exceeds max 10,000,000
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(reviewedLoan));
    when(userRepository.findByEmail(approverUsername)).thenReturn(Optional.of(approver));
    doNothing().when(roleActionGuard).validate(any(User.class), eq("approve"));
    doNothing().when(branchAccessGuard).validate(any(User.class), any(Loan.class));
    doNothing().when(loanActionValidator).validate(any(Loan.class), eq("approve"));

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> approveLoanUseCase.execute(loanId, approverUsername, "Approved"));
    assertTrue(exception.getMessage().contains("exceeds available plafond"));
  }

  @Test
  @DisplayName("Execute should throw exception when customer already has an approved loan")
  void execute_ShouldThrowException_WhenCustomerHasApprovedLoan() {
    // Arrange
    Loan existingApprovedLoan =
        Loan.builder()
            .id(UUID.randomUUID())
            .customer(customer)
            .loanStatus(LoanStatus.APPROVED)
            .build();

    when(loanRepository.findById(loanId)).thenReturn(Optional.of(reviewedLoan));
    when(userRepository.findByEmail(approverUsername)).thenReturn(Optional.of(approver));
    doNothing().when(roleActionGuard).validate(any(User.class), eq("approve"));
    doNothing().when(branchAccessGuard).validate(any(User.class), any(Loan.class));
    doNothing().when(loanActionValidator).validate(any(Loan.class), eq("approve"));
    when(loanRepository.findByCustomerId(customerId))
        .thenReturn(Collections.singletonList(existingApprovedLoan));

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> approveLoanUseCase.execute(loanId, approverUsername, "Approved"));
    assertEquals("Customer already has an active or approved loan", exception.getMessage());
  }
}
