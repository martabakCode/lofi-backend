package com.lofi.lofiapps.service.impl.usecase.loan;

import com.lofi.lofiapps.dto.request.MarketingApplyLoanRequest;
import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.*;
import com.lofi.lofiapps.enums.ApprovalStage;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.enums.RoleName;
import com.lofi.lofiapps.enums.UserStatus;
import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.repository.*;
import com.lofi.lofiapps.service.impl.factory.ApprovalHistoryFactory;
import com.lofi.lofiapps.service.impl.validator.RiskValidator;
import com.lofi.lofiapps.service.impl.validator.UserBiodataValidator;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketingDraftLoanUseCase {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final BranchRepository branchRepository;
  private final ProductRepository productRepository;
  private final LoanRepository loanRepository;
  private final ApprovalHistoryFactory approvalHistoryFactory;
  private final LoanDtoMapper loanDtoMapper;
  private final PasswordEncoder passwordEncoder;
  private final UserBiodataValidator userBiodataValidator;
  private final RiskValidator riskValidator;

  @Transactional
  public LoanResponse execute(MarketingApplyLoanRequest request, String marketingUsername) {
    // 1. Find or Create User
    User user = userRepository.findByEmail(request.getEmail()).orElse(null);

    if (user == null) {
      user = userRepository.findByUsername(request.getUsername()).orElse(null);
    }

    if (user == null) {
      // Create new user
      Branch branch =
          branchRepository
              .findById(request.getBranchId())
              .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

      Role customerRole =
          roleRepository
              .findByName(RoleName.ROLE_CUSTOMER)
              .orElseThrow(() -> new RuntimeException("Error: Role CUSTOMER is not found."));

      Set<Role> roles = new HashSet<>();
      roles.add(customerRole);

      // Default password: Date of Birth (YYYYMMDD)
      String defaultPassword =
          request.getDateOfBirth().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

      user =
          User.builder()
              .fullName(request.getFullName())
              .email(request.getEmail())
              .username(request.getUsername())
              .phoneNumber(request.getPhoneNumber())
              .password(passwordEncoder.encode(defaultPassword))
              .branch(branch)
              .status(UserStatus.ACTIVE)
              .roles(roles)
              .profileCompleted(true)
              .build();
    } else {
      // Update existing user basic Info if needed
      user.setFullName(request.getFullName());
      user.setPhoneNumber(request.getPhoneNumber());
      user.setProfileCompleted(true);
    }

    if (!Boolean.TRUE.equals(user.getPinSet())) {
      throw new IllegalStateException("Customer must set their PIN before creating a loan draft");
    }

    // Check if user already has a product assigned
    if (user.getProduct() != null) {
      throw new IllegalStateException(
          "User already has an assigned product. Cannot apply for a new loan.");
    }

    // 2. Handle Biodata
    UserBiodata biodata = user.getUserBiodata();
    if (biodata == null) {
      biodata = new UserBiodata();
      user.setUserBiodata(biodata);
      biodata.setUser(user);
    }

    biodata.setIncomeSource(request.getIncomeSource());
    biodata.setIncomeType(request.getIncomeType());
    biodata.setMonthlyIncome(request.getMonthlyIncome());
    biodata.setNik(request.getNik());
    biodata.setDateOfBirth(request.getDateOfBirth());
    biodata.setPlaceOfBirth(request.getPlaceOfBirth());
    biodata.setCity(request.getCity());
    biodata.setAddress(request.getAddress());
    biodata.setProvince(request.getProvince());
    biodata.setDistrict(request.getDistrict());
    biodata.setSubDistrict(request.getSubDistrict());
    biodata.setPostalCode(request.getPostalCode());
    biodata.setGender(request.getGender());
    biodata.setMaritalStatus(request.getMaritalStatus());
    biodata.setOccupation(request.getOccupation());

    // Validate biodata completeness
    userBiodataValidator.validateComplete(biodata);

    // 2.5 Assign lowest product to user if not assigned
    if (user.getProduct() == null) {
      productRepository.findTopByIsActiveTrueOrderByMinLoanAmountAsc().ifPresent(user::setProduct);
    }

    user = userRepository.save(user);

    // 3. Create Loan with DRAFT status
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

    // Risk Condition Check
    riskValidator.validate(user, biodata, request.getLoanAmount());

    Loan loan =
        Loan.builder()
            .loanAmount(request.getLoanAmount())
            .tenor(request.getTenor())
            .loanStatus(LoanStatus.DRAFT)
            .currentStage(ApprovalStage.CUSTOMER)
            .customer(user)
            .product(product)
            .branch(user.getBranch())
            .submittedAt(null)
            .purpose(request.getPurpose())
            .bankName(request.getBankName())
            .bankBranch(request.getBankBranch())
            .accountNumber(request.getAccountNumber())
            .accountHolderName(request.getAccountHolderName())
            // Snapshot product rates at loan creation
            .interestRate(product.getInterestRate())
            .adminFee(product.getAdminFee())
            .build();

    Loan savedLoan = loanRepository.save(loan);

    // Save history - record as DRAFT
    approvalHistoryFactory.recordStatusChange(
        savedLoan.getId(),
        null,
        LoanStatus.DRAFT,
        marketingUsername,
        "Loan Draft Created by Marketing on behalf of Customer");

    return loanDtoMapper.toResponse(savedLoan);
  }
}
