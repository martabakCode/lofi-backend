package com.lofi.lofiapps.service.impl.usecase.loan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.request.LoanRequest;
import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.entity.Product;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.entity.UserBiodata;
import com.lofi.lofiapps.enums.ApprovalStage;
import com.lofi.lofiapps.enums.JobType;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.enums.UserStatus;
import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.repository.ProductRepository;
import com.lofi.lofiapps.repository.UserBiodataRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.impl.calculator.PlafondCalculator;
import com.lofi.lofiapps.service.impl.factory.ApprovalHistoryFactory;
import com.lofi.lofiapps.service.impl.validator.RiskValidator;
import com.lofi.lofiapps.service.impl.validator.UserBiodataValidator;
import java.math.BigDecimal;
import java.time.LocalDate;
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
class ApplyLoanUseCaseTest {

  @Mock private LoanRepository loanRepository;
  @Mock private UserRepository userRepository;
  @Mock private ProductRepository productRepository;
  @Mock private UserBiodataRepository userBiodataRepository;
  @Mock private LoanDtoMapper loanDtoMapper;
  @Mock private UserBiodataValidator userBiodataValidator;
  @Mock private RiskValidator riskValidator;
  @Mock private PlafondCalculator plafondCalculator;
  @Mock private ApprovalHistoryFactory approvalHistoryFactory;

  @InjectMocks private ApplyLoanUseCase applyLoanUseCase;

  private UUID userId;
  private String username;
  private User customer;
  private Product product;
  private UserBiodata userBiodata;
  private LoanRequest loanRequest;
  private LoanResponse expectedResponse;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    username = "testuser";

    customer =
        User.builder()
            .id(userId)
            .username(username)
            .email("test@example.com")
            .status(UserStatus.ACTIVE)
            .profileCompleted(true)
            .build();

    product =
        Product.builder()
            .id(UUID.randomUUID())
            .productName("Test Product")
            .minLoanAmount(BigDecimal.valueOf(1000000))
            .maxLoanAmount(BigDecimal.valueOf(10000000))
            .maxTenor(24)
            .interestRate(BigDecimal.valueOf(10))
            .adminFee(BigDecimal.valueOf(50000))
            .build();

    customer.setProduct(product);

    userBiodata =
        UserBiodata.builder()
            .id(UUID.randomUUID())
            .user(customer)
            .nik("1234567890123456")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .build();

    loanRequest =
        LoanRequest.builder()
            .loanAmount(BigDecimal.valueOf(5000000))
            .tenor(12)
            .jobType(JobType.KARYAWAN)
            .companyName("Test Company")
            .build();

    expectedResponse =
        LoanResponse.builder()
            .id(UUID.randomUUID())
            .loanAmount(BigDecimal.valueOf(5000000))
            .tenor(12)
            .loanStatus(LoanStatus.DRAFT)
            .build();
  }

  @Test
  @DisplayName("Execute should create loan successfully with all requirements met")
  void execute_ShouldCreateLoanSuccessfully() {
    // Arrange
    when(userRepository.findById(userId)).thenReturn(Optional.of(customer));
    when(userBiodataValidator.validateAndGet(userId)).thenReturn(userBiodata);
    when(plafondCalculator.calculateAvailablePlafond(customer, product))
        .thenReturn(BigDecimal.valueOf(10000000));
    doNothing().when(riskValidator).validate(any(User.class), any(UserBiodata.class), any());

    Loan savedLoan =
        Loan.builder()
            .id(UUID.randomUUID())
            .customer(customer)
            .product(product)
            .loanAmount(BigDecimal.valueOf(5000000))
            .tenor(12)
            .loanStatus(LoanStatus.DRAFT)
            .currentStage(ApprovalStage.CUSTOMER)
            .interestRate(product.getInterestRate())
            .adminFee(product.getAdminFee())
            .build();

    when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);
    when(loanDtoMapper.toResponse(any(Loan.class))).thenReturn(expectedResponse);
    when(approvalHistoryFactory.recordStatusChange(any(), any(), any(), any(), any()))
        .thenReturn(null);

    // Act
    LoanResponse result = applyLoanUseCase.execute(loanRequest, userId, username);

    // Assert
    assertNotNull(result);
    verify(loanRepository).save(any(Loan.class));
    verify(approvalHistoryFactory)
        .recordStatusChange(any(), isNull(), eq(LoanStatus.DRAFT), eq(username), anyString());
  }

  @Test
  @DisplayName("Execute should throw exception when loan amount is null")
  void execute_ShouldThrowException_WhenLoanAmountIsNull() {
    // Arrange
    loanRequest.setLoanAmount(null);

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> applyLoanUseCase.execute(loanRequest, userId, username));
    assertEquals("Loan amount and tenor are required", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when tenor is null")
  void execute_ShouldThrowException_WhenTenorIsNull() {
    // Arrange
    loanRequest.setTenor(null);

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> applyLoanUseCase.execute(loanRequest, userId, username));
    assertEquals("Loan amount and tenor are required", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when user not found")
  void execute_ShouldThrowException_WhenUserNotFound() {
    // Arrange
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> applyLoanUseCase.execute(loanRequest, userId, username));
    assertEquals("User not found", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when user is not active")
  void execute_ShouldThrowException_WhenUserNotActive() {
    // Arrange
    customer.setStatus(UserStatus.INACTIVE);
    when(userRepository.findById(userId)).thenReturn(Optional.of(customer));

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> applyLoanUseCase.execute(loanRequest, userId, username));
    assertEquals("User is not active", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when user profile is incomplete")
  void execute_ShouldThrowException_WhenProfileIncomplete() {
    // Arrange
    customer.setProfileCompleted(false);
    when(userRepository.findById(userId)).thenReturn(Optional.of(customer));

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> applyLoanUseCase.execute(loanRequest, userId, username));
    assertEquals(
        "User profile is incomplete. Please complete your profile first.", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when user has no assigned product")
  void execute_ShouldThrowException_WhenNoProductAssigned() {
    // Arrange
    customer.setProduct(null);
    when(userRepository.findById(userId)).thenReturn(Optional.of(customer));
    when(userBiodataValidator.validateAndGet(userId)).thenReturn(userBiodata);

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> applyLoanUseCase.execute(loanRequest, userId, username));
    assertEquals(
        "User does not have an assigned product. Please assign a product first.",
        exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when loan amount is below minimum")
  void execute_ShouldThrowException_WhenLoanAmountBelowMinimum() {
    // Arrange
    loanRequest.setLoanAmount(BigDecimal.valueOf(500000)); // Below min 1,000,000
    when(userRepository.findById(userId)).thenReturn(Optional.of(customer));
    when(userBiodataValidator.validateAndGet(userId)).thenReturn(userBiodata);

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> applyLoanUseCase.execute(loanRequest, userId, username));
    assertTrue(exception.getMessage().contains("Loan amount is less than minimum"));
  }

  @Test
  @DisplayName("Execute should throw exception when loan amount exceeds maximum")
  void execute_ShouldThrowException_WhenLoanAmountExceedsMaximum() {
    // Arrange
    loanRequest.setLoanAmount(BigDecimal.valueOf(20000000)); // Above max 10,000,000
    when(userRepository.findById(userId)).thenReturn(Optional.of(customer));
    when(userBiodataValidator.validateAndGet(userId)).thenReturn(userBiodata);

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> applyLoanUseCase.execute(loanRequest, userId, username));
    assertTrue(exception.getMessage().contains("Loan amount exceeds maximum"));
  }

  @Test
  @DisplayName("Execute should throw exception when tenor exceeds maximum")
  void execute_ShouldThrowException_WhenTenorExceedsMaximum() {
    // Arrange
    loanRequest.setTenor(36); // Above max 24
    when(userRepository.findById(userId)).thenReturn(Optional.of(customer));
    when(userBiodataValidator.validateAndGet(userId)).thenReturn(userBiodata);

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> applyLoanUseCase.execute(loanRequest, userId, username));
    assertTrue(exception.getMessage().contains("Tenor exceeds maximum"));
  }

  @Test
  @DisplayName("Execute should throw exception when loan amount exceeds available plafond")
  void execute_ShouldThrowException_WhenLoanAmountExceedsPlafond() {
    // Arrange
    when(userRepository.findById(userId)).thenReturn(Optional.of(customer));
    when(userBiodataValidator.validateAndGet(userId)).thenReturn(userBiodata);
    when(plafondCalculator.calculateAvailablePlafond(customer, product))
        .thenReturn(BigDecimal.valueOf(3000000)); // Less than requested 5,000,000

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> applyLoanUseCase.execute(loanRequest, userId, username));
    assertTrue(exception.getMessage().contains("Loan amount exceeds available plafond"));
  }
}
