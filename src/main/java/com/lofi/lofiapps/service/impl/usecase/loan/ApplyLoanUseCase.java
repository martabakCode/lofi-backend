package com.lofi.lofiapps.service.impl.usecase.loan;

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
import com.lofi.lofiapps.repository.UserBiodataRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.NotificationService;
import com.lofi.lofiapps.service.impl.calculator.PlafondCalculator;
import com.lofi.lofiapps.service.impl.factory.ApprovalHistoryFactory;
import com.lofi.lofiapps.service.impl.validator.RiskValidator;
import com.lofi.lofiapps.service.impl.validator.UserBiodataValidator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplyLoanUseCase {

  private final LoanRepository loanRepository;
  private final UserRepository userRepository;

  private final UserBiodataRepository userBiodataRepository;
  private final LoanDtoMapper loanDtoMapper;
  private final UserBiodataValidator userBiodataValidator;
  private final RiskValidator riskValidator;
  private final PlafondCalculator plafondCalculator;
  private final ApprovalHistoryFactory approvalHistoryFactory;
  private final NotificationService notificationService;
  private final com.lofi.lofiapps.service.impl.usecase.pin.ValidatePinUseCase validatePinUseCase;

  @Transactional
  public LoanResponse execute(LoanRequest request, UUID userId, String username) {
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

    if (!Boolean.TRUE.equals(user.getPinSet())) {
      throw new IllegalStateException("Please set your PIN before applying for a loan");
    }

    if (!Boolean.TRUE.equals(user.getProfileCompleted())) {
      throw new IllegalStateException(
          "User profile is incomplete. Please complete your profile first.");
    }

    // Check if user biodata is complete using validator
    UserBiodata userBiodata = userBiodataValidator.validateAndGet(userId);

    // Get product from user (user must have a product assigned)
    Product product = user.getProduct();
    if (product == null) {
      throw new IllegalStateException(
          "User does not have an assigned product. Please assign a product first.");
    }

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

    // Validate against available plafond using calculator
    BigDecimal availablePlafond = plafondCalculator.calculateAvailablePlafond(user, product);
    if (request.getLoanAmount().compareTo(availablePlafond) > 0) {
      throw new IllegalArgumentException(
          "Loan amount exceeds available plafond. Available: "
              + availablePlafond
              + ", Requested: "
              + request.getLoanAmount());
    }

    // Risk Condition Check using validator
    riskValidator.validate(user, userBiodata, request.getLoanAmount());

    // Calculate age at loan application and completion
    int ageAtApplication = calculateAge(userBiodata.getDateOfBirth());
    int ageAtCompletion =
        calculateAgeAtCompletion(userBiodata.getDateOfBirth(), request.getTenor());

    // Validate age requirements
    validateAgeRequirements(ageAtCompletion, request.getJobType());

    // Validate PIN if provided
    boolean pinValidated = false;
    if (request.getPin() != null && !request.getPin().isEmpty()) {
      validatePinUseCase.execute(
          request.getPin(), userId, null); // passing null for IP address for now as it's
      // optional/not readily available here without extra
      // work
      pinValidated = true;
    }

    // Create Loan with SUBMITTED status (direct submit)
    Loan loan =
        Loan.builder()
            .loanAmount(request.getLoanAmount())
            .tenor(request.getTenor())
            .loanStatus(LoanStatus.SUBMITTED)
            .currentStage(ApprovalStage.MARKETING)
            .customer(user)
            .product(product)
            .branch(user.getBranch())
            .submittedAt(LocalDateTime.now())
            .lastStatusChangedAt(LocalDateTime.now())
            .longitude(request.getLongitude())
            .latitude(request.getLatitude())
            .declaredIncome(request.getDeclaredIncome())
            .npwpNumber(request.getNpwpNumber())
            .jobType(request.getJobType())
            .companyName(request.getCompanyName())
            .jobPosition(request.getJobPosition())
            .workDurationMonths(request.getWorkDurationMonths())
            .workAddress(request.getWorkAddress())
            .officePhoneNumber(request.getOfficePhoneNumber())
            .additionalIncome(request.getAdditionalIncome())
            .emergencyContactName(request.getEmergencyContactName())
            .emergencyContactRelation(request.getEmergencyContactRelation())
            .emergencyContactPhone(request.getEmergencyContactPhone())
            .emergencyContactAddress(request.getEmergencyContactAddress())
            .downPayment(request.getDownPayment())
            .purpose(request.getPurpose())
            .bankName(request.getBankName())
            .bankBranch(request.getBankBranch())
            .accountNumber(request.getAccountNumber())
            .accountHolderName(request.getAccountHolderName())
            // Snapshot product rates at loan creation
            .interestRate(product.getInterestRate())
            .adminFee(product.getAdminFee())
            .pinValidated(pinValidated)
            .build();

    // Update user biodata with new fields and age info
    updateUserBiodata(userBiodata, request, ageAtApplication, ageAtCompletion);

    Loan savedLoan = loanRepository.save(loan);

    // Save history using factory - record as SUBMITTED (not DRAFT)
    approvalHistoryFactory.recordStatusChange(
        savedLoan.getId(), null, LoanStatus.SUBMITTED, username, "Loan application submitted");

    // Send notification
    notificationService.notifyLoanStatusChange(user.getId(), LoanStatus.SUBMITTED);

    return loanDtoMapper.toResponse(savedLoan);
  }

  private int calculateAge(LocalDate dateOfBirth) {
    if (dateOfBirth == null) return 0;
    return Period.between(dateOfBirth, LocalDate.now()).getYears();
  }

  private int calculateAgeAtCompletion(LocalDate dateOfBirth, Integer tenorMonths) {
    if (dateOfBirth == null || tenorMonths == null) return 0;
    LocalDate completionDate = LocalDate.now().plusMonths(tenorMonths);
    return Period.between(dateOfBirth, completionDate).getYears();
  }

  private void validateAgeRequirements(int ageAtCompletion, JobType jobType) {
    // Minimum age: 21 years
    int currentAge = calculateAge(LocalDate.now().minusYears(ageAtCompletion));
    if (currentAge < 21) {
      throw new IllegalStateException(
          "Age requirement not met: Minimum age is 21 years. Loan application rejected.");
    }

    // Maximum age depends on job type
    int maxAge;
    if (jobType == JobType.WIRASWASTA) {
      maxAge = 65; // Wirausaha: 60-65
    } else {
      maxAge = 60; // Karyawan/Profesional: 55-60
    }

    if (ageAtCompletion > maxAge) {
      throw new IllegalStateException(
          "Age requirement not met: Maximum age at loan completion is "
              + maxAge
              + " years for "
              + (jobType != null ? jobType.name() : "this job type")
              + ". Loan application rejected.");
    }
  }

  private void updateUserBiodata(
      UserBiodata userBiodata, LoanRequest request, int ageAtApplication, int ageAtCompletion) {
    // Update job-related fields
    if (request.getJobType() != null) {
      userBiodata.setJobType(request.getJobType());
    }
    if (request.getCompanyName() != null) {
      userBiodata.setCompanyName(request.getCompanyName());
    }
    if (request.getJobPosition() != null) {
      userBiodata.setJobPosition(request.getJobPosition());
    }
    if (request.getWorkDurationMonths() != null) {
      userBiodata.setWorkDurationMonths(request.getWorkDurationMonths());
    }
    if (request.getWorkAddress() != null) {
      userBiodata.setWorkAddress(request.getWorkAddress());
    }
    if (request.getOfficePhoneNumber() != null) {
      userBiodata.setOfficePhoneNumber(request.getOfficePhoneNumber());
    }
    if (request.getAdditionalIncome() != null) {
      userBiodata.setAdditionalIncome(request.getAdditionalIncome());
    }
    if (request.getEmergencyContactName() != null) {
      userBiodata.setEmergencyContactName(request.getEmergencyContactName());
    }
    if (request.getEmergencyContactRelation() != null) {
      userBiodata.setEmergencyContactRelation(request.getEmergencyContactRelation());
    }
    if (request.getEmergencyContactPhone() != null) {
      userBiodata.setEmergencyContactPhone(request.getEmergencyContactPhone());
    }
    if (request.getEmergencyContactAddress() != null) {
      userBiodata.setEmergencyContactAddress(request.getEmergencyContactAddress());
    }
    if (request.getNpwpNumber() != null) {
      userBiodata.setNpwpNumber(request.getNpwpNumber());
    }

    // Update age tracking
    userBiodata.setAgeAtLoanApplication(ageAtApplication);
    userBiodata.setAgeAtLoanCompletion(ageAtCompletion);

    userBiodataRepository.save(userBiodata);
  }
}
