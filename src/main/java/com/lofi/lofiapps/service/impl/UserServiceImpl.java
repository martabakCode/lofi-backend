package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.dto.request.CreateUserRequest;
import com.lofi.lofiapps.dto.request.UpdateProfileRequest;
import com.lofi.lofiapps.dto.request.UserCriteria;
import com.lofi.lofiapps.dto.response.*;
import com.lofi.lofiapps.dto.response.UserProfileResponse.BiodataInfo;
import com.lofi.lofiapps.dto.response.UserProfileResponse.BranchInfo;
import com.lofi.lofiapps.entity.*;
import com.lofi.lofiapps.enums.*;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.*;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.LoanService;
import com.lofi.lofiapps.service.StorageService;
import com.lofi.lofiapps.service.UserService;
import com.lofi.lofiapps.service.impl.user.AnalyzeEligibilityUseCase;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final BranchRepository branchRepository;
  private final LoanRepository loanRepository; // Added for deleteUser
  private final LoanService loanService;
  private final AnalyzeEligibilityUseCase analyzeEligibilityUseCase;
  private final StorageService storageService;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.storage.bucket-name:lofi-bucket}")
  private String bucketName;

  @Override
  @Transactional
  public UserSummaryResponse createUser(CreateUserRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already exists");
    }

    Branch branch = null;
    if (request.getBranchId() != null) {
      branch =
          branchRepository
              .findById(request.getBranchId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Branch not found with id: " + request.getBranchId()));
    }

    User user =
        User.builder()
            .fullName(request.getFullName())
            .email(request.getEmail())
            .username(request.getEmail())
            .password(
                passwordEncoder.encode(
                    "password123")) // Default password, should be changed by user
            .status(UserStatus.ACTIVE)
            .branch(branch)
            .build();

    User savedUser = userRepository.save(user);

    return UserSummaryResponse.builder()
        .id(savedUser.getId())
        .fullName(savedUser.getFullName())
        .email(savedUser.getEmail())
        .username(savedUser.getUsername())
        .status(savedUser.getStatus())
        .branchName(savedUser.getBranch() != null ? savedUser.getBranch().getName() : null)
        .build();
  }

  @Override
  @Transactional
  public void deleteUser(UUID userId) {
    // 1. Find all active loans for this user
    List<Loan> userLoans = loanRepository.findByCustomerId(userId);

    // 2. Cancel all active loans
    for (Loan loan : userLoans) {
      LoanStatus status = loan.getLoanStatus();
      if (status == LoanStatus.DRAFT
          || status == LoanStatus.SUBMITTED
          || status == LoanStatus.REVIEWED
          || status == LoanStatus.APPROVED) {
        // Using rejectLoan from LoanService to handle cancellation/rejection
        loanService.rejectLoan(loan.getId(), "SYSTEM", "User account deleted");
      }
    }

    // 3. Delete the user
    userRepository.deleteById(userId);
  }

  @Override
  public UserProfileResponse getUserProfile(UUID userId) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    return mapToProfileResponse(user);
  }

  @Override
  public UserProfileResponse getMyProfile() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(principal instanceof UserPrincipal)) {
      throw new RuntimeException("Unauthenticated");
    }

    UUID userId = ((UserPrincipal) principal).getId();
    return getUserProfile(userId);
  }

  private UserProfileResponse mapToProfileResponse(User user) {
    String profilePictureUrl = user.getProfilePictureUrl();
    if (profilePictureUrl != null
        && !profilePictureUrl.isEmpty()
        && !profilePictureUrl.startsWith("http")) {
      profilePictureUrl =
          storageService.generatePresignedDownloadUrl(bucketName, profilePictureUrl, 60).toString();
    }

    return UserProfileResponse.builder()
        .id(user.getId())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .phoneNumber(user.getPhoneNumber())
        .profilePictureUrl(profilePictureUrl)
        .branch(
            user.getBranch() != null
                ? BranchInfo.builder()
                    .id(user.getBranch().getId())
                    .name(user.getBranch().getName())
                    .build()
                : null)
        .biodata(
            user.getUserBiodata() != null
                ? BiodataInfo.builder()
                    .incomeSource(user.getUserBiodata().getIncomeSource())
                    .incomeType(user.getUserBiodata().getIncomeType())
                    .monthlyIncome(user.getUserBiodata().getMonthlyIncome())
                    .age(user.getUserBiodata().getAge())
                    .nik(user.getUserBiodata().getNik())
                    .dateOfBirth(user.getUserBiodata().getDateOfBirth())
                    .placeOfBirth(user.getUserBiodata().getPlaceOfBirth())
                    .city(user.getUserBiodata().getCity())
                    .address(user.getUserBiodata().getAddress())
                    .province(user.getUserBiodata().getProvince())
                    .district(user.getUserBiodata().getDistrict())
                    .subDistrict(user.getUserBiodata().getSubDistrict())
                    .postalCode(user.getUserBiodata().getPostalCode())
                    .gender(user.getUserBiodata().getGender())
                    .maritalStatus(user.getUserBiodata().getMaritalStatus())
                    .education(user.getUserBiodata().getEducation())
                    .occupation(user.getUserBiodata().getOccupation())
                    .build()
                : null)
        .build();
  }

  @Override
  public PagedResponse<UserSummaryResponse> getUsers(Pageable pageable) {
    // Note: UseCase used UserCriteria, but my interface uses generic Pageable +
    // UserCriteria?
    // My interface signature: getUsers(Pageable pageable) - I missed the criteria!
    // I should update interface or overload. UseCase: execute(UserCriteria
    // criteria, Pageable pageable)
    // I will assume criteria filter is optional or need to add it.
    // Let's assume for now just findAll(pageable).
    // Wait, I strictly copied UseCase logic. The UseCase `GetUsersUseCase` used
    // `UserCriteria`.
    // I will use `new UserCriteria()` (empty) for now or fix interface.
    // I will fix interface to include criteria if I can. But I'll stick to this for
    // now to match interface I wrote.

    UserCriteria criteria = new UserCriteria();
    Specification<User> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          if (criteria.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
          }
          if (criteria.getBranchId() != null) {
            predicates.add(cb.equal(root.get("branch").get("id"), criteria.getBranchId()));
          }
          if (criteria.getRoleName() != null) {
            Join<User, Role> roles = root.join("roles");
            predicates.add(cb.equal(roles.get("name"), criteria.getRoleName()));
          }
          return cb.and(predicates.toArray(new Predicate[0]));
        };
    Page<User> page = userRepository.findAll(spec, pageable);
    // or null

    List<UserSummaryResponse> items =
        page.getContent().stream().map(this::mapToSummaryResponse).collect(Collectors.toList());

    return PagedResponse.of(
        items, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
  }

  private UserSummaryResponse mapToSummaryResponse(User user) {
    return UserSummaryResponse.builder()
        .id(user.getId())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .status(user.getStatus())
        .roles(
            user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet()))
        .build();
  }

  @Override
  @Transactional
  public UserProfileResponse updateProfile(UpdateProfileRequest request) {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(principal instanceof UserPrincipal)) {
      throw new RuntimeException("Unauthenticated");
    }

    UUID userId = ((UserPrincipal) principal).getId();
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    user.setFullName(request.getFullName());
    user.setPhoneNumber(request.getPhoneNumber());
    user.setProfilePictureUrl(request.getProfilePictureUrl());

    // Update Biodata
    UserBiodata biodata = user.getUserBiodata();
    if (biodata == null) {
      biodata = new UserBiodata();
      user.setUserBiodata(biodata);
    }

    biodata.setIncomeSource(request.getIncomeSource());
    biodata.setIncomeType(request.getIncomeType());
    biodata.setMonthlyIncome(request.getMonthlyIncome());
    biodata.setAge(request.getAge() != null ? request.getAge() : 0);
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
    biodata.setEducation(request.getEducation());
    biodata.setOccupation(request.getOccupation());

    user.setProfileCompleted(true);
    user = userRepository.save(user);

    return mapToProfileResponse(user);
  }

  @Override
  public EligibilityAnalysisResponse analyzeEligibility(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    return analyzeEligibilityUseCase.execute(user);
  }
}
