package com.lofi.lofiapps.service.impl.usecase.user;

import com.lofi.lofiapps.dto.response.UserProfileResponse;
import com.lofi.lofiapps.dto.response.UserProfileResponse.BiodataInfo;
import com.lofi.lofiapps.dto.response.UserProfileResponse.BranchInfo;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.impl.usecase.storage.R2StorageService;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUserProfileUseCase {

  private final UserRepository userRepository;
  private final R2StorageService storageService;
  private final com.lofi.lofiapps.service.ProductCalculationService productCalculationService;

  @Value("${app.storage.bucket-name:lofi-bucket}")
  private String bucketName;

  public UserProfileResponse execute(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
    return mapToProfileResponse(user);
  }

  public UserProfileResponse mapToProfileResponse(User user) {
    String profilePictureUrl = user.getProfilePictureUrl();
    if (profilePictureUrl != null
        && !profilePictureUrl.isEmpty()
        && !profilePictureUrl.startsWith("http")) {
      profilePictureUrl =
          storageService.generatePresignedDownloadUrl(bucketName, profilePictureUrl, 60).toString();
    }

    BigDecimal availablePlafond = BigDecimal.ZERO;
    BigDecimal totalApprovedLoans = BigDecimal.ZERO;
    Boolean hasActiveLoan = false;

    if (user.getProduct() != null) {
      availablePlafond =
          productCalculationService.calculateAvailableAmount(
              user.getId(), user.getProduct().getId());
      hasActiveLoan = productCalculationService.hasActiveLoan(user.getId());

      totalApprovedLoans = productCalculationService.calculateTotalApprovedLoanAmount(user.getId());
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
                    .occupation(user.getUserBiodata().getOccupation())
                    .build()
                : null)
        .product(
            user.getProduct() != null
                ? com.lofi.lofiapps.dto.response.ProductResponse.builder()
                    .id(user.getProduct().getId())
                    .productCode(user.getProduct().getProductCode())
                    .productName(user.getProduct().getProductName())
                    .interestRate(user.getProduct().getInterestRate())
                    .minTenor(user.getProduct().getMinTenor())
                    .maxTenor(user.getProduct().getMaxTenor())
                    .minLoanAmount(user.getProduct().getMinLoanAmount())
                    .maxLoanAmount(user.getProduct().getMaxLoanAmount())
                    .adminFee(user.getProduct().getAdminFee())
                    .build()
                : null)
        .availablePlafond(availablePlafond)
        .totalApprovedLoans(totalApprovedLoans)
        .hasActiveLoan(hasActiveLoan)
        .pinSet(user.getPinSet())
        .profileCompleted(user.getProfileCompleted())
        .build();
  }
}
