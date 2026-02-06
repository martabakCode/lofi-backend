package com.lofi.lofiapps.service.impl.usecase.loan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.service.NotificationService;
import com.lofi.lofiapps.service.impl.factory.ApprovalHistoryFactory;
import java.math.BigDecimal;
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
class DisburseLoanUseCaseTest {

  @Mock private LoanRepository loanRepository;
  @Mock private ApprovalHistoryFactory approvalHistoryFactory;
  @Mock private NotificationService notificationService;
  @Mock private LoanDtoMapper loanDtoMapper;

  @InjectMocks private DisburseLoanUseCase disburseLoanUseCase;

  private UUID loanId;
  private UUID customerId;
  private String officerUsername;
  private Loan approvedLoan;
  private User customer;
  private LoanResponse expectedResponse;

  @BeforeEach
  void setUp() {
    loanId = UUID.randomUUID();
    customerId = UUID.randomUUID();
    officerUsername = "officer@example.com";

    customer =
        User.builder().id(customerId).email("customer@example.com").username("customer").build();

    approvedLoan =
        Loan.builder()
            .id(loanId)
            .customer(customer)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.APPROVED)
            .bankName("Bank Test")
            .accountNumber("1234567890")
            .accountHolderName("Customer Name")
            .build();

    expectedResponse =
        LoanResponse.builder()
            .id(loanId)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.DISBURSED)
            .build();
  }

  @Test
  @DisplayName("Execute should disburse loan successfully when loan is approved")
  void execute_ShouldDisburseLoanSuccessfully() {
    // Arrange
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(approvedLoan));

    Loan savedLoan =
        Loan.builder()
            .id(loanId)
            .customer(customer)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.DISBURSED)
            .disbursementReference("REF123")
            .build();

    when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);
    when(loanDtoMapper.toResponse(any(Loan.class))).thenReturn(expectedResponse);
    when(approvalHistoryFactory.recordStatusChange(any(UUID.class), any(), any(), any(), any()))
        .thenReturn(null);
    doNothing().when(notificationService).notifyLoanDisbursement(any(Loan.class));

    // Act
    LoanResponse result = disburseLoanUseCase.execute(loanId, officerUsername, "REF123");

    // Assert
    assertNotNull(result);
    assertEquals(LoanStatus.DISBURSED, result.getLoanStatus());
    verify(loanRepository).save(any(Loan.class));
    verify(approvalHistoryFactory).recordStatusChange(any(UUID.class), any(), any(), any(), any());
    verify(notificationService).notifyLoanDisbursement(any(Loan.class));
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
            () -> disburseLoanUseCase.execute(loanId, officerUsername, "REF123"));
    assertEquals("Loan", exception.getResourceName());
  }

  @Test
  @DisplayName("Execute should throw exception when loan is not in APPROVED status")
  void execute_ShouldThrowException_WhenLoanNotApproved() {
    // Arrange
    approvedLoan.setLoanStatus(LoanStatus.REVIEWED);
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(approvedLoan));

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> disburseLoanUseCase.execute(loanId, officerUsername, "REF123"));
    assertEquals("Only approved loans can be disbursed", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should complete successfully even when notification fails")
  void execute_ShouldComplete_WhenNotificationFails() {
    // Arrange
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(approvedLoan));

    Loan savedLoan =
        Loan.builder()
            .id(loanId)
            .customer(customer)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.DISBURSED)
            .build();

    when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);
    when(loanDtoMapper.toResponse(any(Loan.class))).thenReturn(expectedResponse);
    when(approvalHistoryFactory.recordStatusChange(any(UUID.class), any(), any(), any(), any()))
        .thenReturn(null);
    doThrow(new RuntimeException("Notification failed"))
        .when(notificationService)
        .notifyLoanDisbursement(any(Loan.class));

    // Act
    LoanResponse result = disburseLoanUseCase.execute(loanId, officerUsername, "REF123");

    // Assert
    assertNotNull(result);
    assertEquals(LoanStatus.DISBURSED, result.getLoanStatus());
    verify(loanRepository).save(any(Loan.class));
  }

  @Test
  @DisplayName("Execute should handle null customer gracefully")
  void execute_ShouldHandleNullCustomer() {
    // Arrange
    approvedLoan.setCustomer(null);
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(approvedLoan));

    Loan savedLoan =
        Loan.builder()
            .id(loanId)
            .customer(null)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.DISBURSED)
            .build();

    when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);
    when(loanDtoMapper.toResponse(any(Loan.class))).thenReturn(expectedResponse);
    when(approvalHistoryFactory.recordStatusChange(any(UUID.class), any(), any(), any(), any()))
        .thenReturn(null);

    // Act
    LoanResponse result = disburseLoanUseCase.execute(loanId, officerUsername, "REF123");

    // Assert
    assertNotNull(result);
    verify(loanRepository).save(any(Loan.class));
    verify(approvalHistoryFactory).recordStatusChange(any(UUID.class), any(), any(), any(), any());
  }
}
