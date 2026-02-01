package com.lofi.lofiapps.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.entity.Product;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.ApprovalStage;
import com.lofi.lofiapps.enums.LoanStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanDtoMapperTest {

  @Mock private ProductDtoMapper productDtoMapper;

  @InjectMocks private LoanDtoMapper loanDtoMapper;

  private UUID loanId;
  private UUID customerId;
  private UUID productId;

  @BeforeEach
  void setUp() {
    loanId = UUID.randomUUID();
    customerId = UUID.randomUUID();
    productId = UUID.randomUUID();
  }

  @Test
  @DisplayName("ToResponse should return null when loan is null")
  void toResponse_ShouldReturnNull_WhenLoanIsNull() {
    // Act
    LoanResponse result = loanDtoMapper.toResponse(null);

    // Assert
    assertNull(result);
  }

  @Test
  @DisplayName("ToResponse should map loan to response correctly")
  void toResponse_ShouldMapLoanCorrectly() {
    // Arrange
    User customer = User.builder().id(customerId).fullName("Test Customer").build();

    Product product =
        Product.builder().id(productId).productCode("PROD001").productName("Personal Loan").build();

    LocalDateTime now = LocalDateTime.now();
    Loan loan =
        Loan.builder()
            .id(loanId)
            .customer(customer)
            .product(product)
            .loanAmount(BigDecimal.valueOf(10000000))
            .tenor(12)
            .loanStatus(LoanStatus.APPROVED)
            .currentStage(ApprovalStage.BACKOFFICE)
            .submittedAt(now.minusDays(5))
            .approvedAt(now.minusDays(1))
            .disbursedAt(now)
            .disbursementReference("DISB-123456")
            .build();

    // Act
    LoanResponse result = loanDtoMapper.toResponse(loan);

    // Assert
    assertNotNull(result);
    assertEquals(loanId, result.getId());
    assertEquals(customerId, result.getCustomerId());
    assertEquals("Test Customer", result.getCustomerName());
    assertEquals(BigDecimal.valueOf(10000000), result.getLoanAmount());
    assertEquals(12, result.getTenor());
    assertEquals(LoanStatus.APPROVED, result.getLoanStatus());
    assertEquals(ApprovalStage.BACKOFFICE, result.getCurrentStage());
    assertNotNull(result.getSubmittedAt());
    assertNotNull(result.getApprovedAt());
    assertNotNull(result.getDisbursedAt());
    assertEquals("DISB-123456", result.getDisbursementReference());
  }

  @Test
  @DisplayName("ToResponse should handle null customer")
  void toResponse_ShouldHandleNullCustomer() {
    // Arrange
    Product product = Product.builder().id(productId).productName("Test Product").build();

    Loan loan =
        Loan.builder()
            .id(loanId)
            .customer(null)
            .product(product)
            .loanAmount(BigDecimal.valueOf(5000000))
            .tenor(6)
            .loanStatus(LoanStatus.SUBMITTED)
            .currentStage(ApprovalStage.MARKETING)
            .build();

    // Act
    LoanResponse result = loanDtoMapper.toResponse(loan);

    // Assert
    assertNotNull(result);
    assertNull(result.getCustomerId());
    assertNull(result.getCustomerName());
  }

  @Test
  @DisplayName("ToResponse should handle null dates")
  void toResponse_ShouldHandleNullDates() {
    // Arrange
    User customer = User.builder().id(customerId).fullName("Test Customer").build();
    Product product = Product.builder().id(productId).productName("Test Product").build();

    Loan loan =
        Loan.builder()
            .id(loanId)
            .customer(customer)
            .product(product)
            .loanAmount(BigDecimal.valueOf(5000000))
            .tenor(6)
            .loanStatus(LoanStatus.SUBMITTED)
            .currentStage(ApprovalStage.MARKETING)
            .submittedAt(null)
            .approvedAt(null)
            .rejectedAt(null)
            .disbursedAt(null)
            .build();

    // Act
    LoanResponse result = loanDtoMapper.toResponse(loan);

    // Assert
    assertNotNull(result);
    assertNull(result.getSubmittedAt());
    assertNull(result.getApprovedAt());
    assertNull(result.getRejectedAt());
    assertNull(result.getDisbursedAt());
  }

  @Test
  @DisplayName("ToResponse should map rejected loan correctly")
  void toResponse_ShouldMapRejectedLoanCorrectly() {
    // Arrange
    User customer = User.builder().id(customerId).fullName("Test Customer").build();
    Product product = Product.builder().id(productId).productName("Test Product").build();

    LocalDateTime now = LocalDateTime.now();
    Loan loan =
        Loan.builder()
            .id(loanId)
            .customer(customer)
            .product(product)
            .loanAmount(BigDecimal.valueOf(5000000))
            .tenor(6)
            .loanStatus(LoanStatus.REJECTED)
            .currentStage(ApprovalStage.BACKOFFICE)
            .submittedAt(now.minusDays(3))
            .rejectedAt(now)
            .build();

    // Act
    LoanResponse result = loanDtoMapper.toResponse(loan);

    // Assert
    assertNotNull(result);
    assertEquals(LoanStatus.REJECTED, result.getLoanStatus());
    assertNotNull(result.getRejectedAt());
    assertNull(result.getApprovedAt());
    assertNull(result.getDisbursedAt());
  }

  @Test
  @DisplayName("ToResponse should map all loan statuses")
  void toResponse_ShouldMapAllLoanStatuses() {
    // Arrange
    User customer = User.builder().id(customerId).fullName("Test Customer").build();
    Product product = Product.builder().id(productId).productName("Test Product").build();

    for (LoanStatus status : LoanStatus.values()) {
      Loan loan =
          Loan.builder()
              .id(UUID.randomUUID())
              .customer(customer)
              .product(product)
              .loanAmount(BigDecimal.valueOf(5000000))
              .tenor(6)
              .loanStatus(status)
              .currentStage(ApprovalStage.MARKETING)
              .build();

      // Act
      LoanResponse result = loanDtoMapper.toResponse(loan);

      // Assert
      assertNotNull(result);
      assertEquals(status, result.getLoanStatus());
    }
  }

  @Test
  @DisplayName("ToResponse should map all approval stages")
  void toResponse_ShouldMapAllApprovalStages() {
    // Arrange
    User customer = User.builder().id(customerId).fullName("Test Customer").build();
    Product product = Product.builder().id(productId).productName("Test Product").build();

    for (ApprovalStage stage : ApprovalStage.values()) {
      Loan loan =
          Loan.builder()
              .id(UUID.randomUUID())
              .customer(customer)
              .product(product)
              .loanAmount(BigDecimal.valueOf(5000000))
              .tenor(6)
              .loanStatus(LoanStatus.SUBMITTED)
              .currentStage(stage)
              .build();

      // Act
      LoanResponse result = loanDtoMapper.toResponse(loan);

      // Assert
      assertNotNull(result);
      assertEquals(stage, result.getCurrentStage());
    }
  }
}
