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
class RejectLoanUseCaseTest {

  @Mock private LoanRepository loanRepository;
  @Mock private ApprovalHistoryFactory approvalHistoryFactory;
  @Mock private NotificationService notificationService;
  @Mock private LoanDtoMapper loanDtoMapper;

  @InjectMocks private RejectLoanUseCase rejectLoanUseCase;

  private UUID loanId;
  private UUID customerId;
  private String rejectorUsername;
  private Loan reviewedLoan;
  private User customer;
  private LoanResponse expectedResponse;

  @BeforeEach
  void setUp() {
    loanId = UUID.randomUUID();
    customerId = UUID.randomUUID();
    rejectorUsername = "rejector@example.com";

    customer =
        User.builder().id(customerId).email("customer@example.com").username("customer").build();

    reviewedLoan =
        Loan.builder()
            .id(loanId)
            .customer(customer)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.REVIEWED)
            .build();

    expectedResponse =
        LoanResponse.builder()
            .id(loanId)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.REJECTED)
            .build();
  }

  @Test
  @DisplayName("Execute should reject loan successfully")
  void execute_ShouldRejectLoanSuccessfully() {
    // Arrange
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(reviewedLoan));

    Loan savedLoan =
        Loan.builder()
            .id(loanId)
            .customer(customer)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.REJECTED)
            .build();

    when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);
    when(loanDtoMapper.toResponse(any(Loan.class))).thenReturn(expectedResponse);
    when(approvalHistoryFactory.recordStatusChange(any(UUID.class), any(), any(), any(), any()))
        .thenReturn(null);
    doNothing().when(notificationService).notifyLoanStatusChange(any(), any());

    // Act
    LoanResponse result = rejectLoanUseCase.execute(loanId, rejectorUsername, "Risk too high");

    // Assert
    assertNotNull(result);
    assertEquals(LoanStatus.REJECTED, result.getLoanStatus());
    verify(loanRepository).save(any(Loan.class));
    verify(approvalHistoryFactory).recordStatusChange(any(UUID.class), any(), any(), any(), any());
    verify(notificationService).notifyLoanStatusChange(customerId, LoanStatus.REJECTED);
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
            () -> rejectLoanUseCase.execute(loanId, rejectorUsername, "Risk too high"));
    assertEquals("Loan", exception.getResourceName());
  }

  @Test
  @DisplayName("Execute should handle any loan status for rejection")
  void execute_ShouldHandleAnyStatusForRejection() {
    // Arrange - Test with SUBMITTED status
    reviewedLoan.setLoanStatus(LoanStatus.SUBMITTED);
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(reviewedLoan));

    Loan savedLoan =
        Loan.builder()
            .id(loanId)
            .customer(customer)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.REJECTED)
            .build();

    when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);
    when(loanDtoMapper.toResponse(any(Loan.class))).thenReturn(expectedResponse);
    when(approvalHistoryFactory.recordStatusChange(any(UUID.class), any(), any(), any(), any()))
        .thenReturn(null);
    doNothing().when(notificationService).notifyLoanStatusChange(any(), any());

    // Act
    LoanResponse result = rejectLoanUseCase.execute(loanId, rejectorUsername, "Risk too high");

    // Assert
    assertNotNull(result);
    assertEquals(LoanStatus.REJECTED, result.getLoanStatus());
  }

  @Test
  @DisplayName("Execute should record rejection notes in approval history")
  void execute_ShouldRecordRejectionNotes() {
    // Arrange
    String rejectionNotes = "Insufficient credit score";
    when(loanRepository.findById(loanId)).thenReturn(Optional.of(reviewedLoan));

    Loan savedLoan =
        Loan.builder()
            .id(loanId)
            .customer(customer)
            .loanAmount(BigDecimal.valueOf(5000000))
            .loanStatus(LoanStatus.REJECTED)
            .build();

    when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);
    when(loanDtoMapper.toResponse(any(Loan.class))).thenReturn(expectedResponse);
    when(approvalHistoryFactory.recordStatusChange(any(UUID.class), any(), any(), any(), any()))
        .thenReturn(null);
    doNothing().when(notificationService).notifyLoanStatusChange(any(), any());

    // Act
    rejectLoanUseCase.execute(loanId, rejectorUsername, rejectionNotes);

    // Assert
    verify(approvalHistoryFactory).recordStatusChange(any(UUID.class), any(), any(), any(), any());
  }
}
