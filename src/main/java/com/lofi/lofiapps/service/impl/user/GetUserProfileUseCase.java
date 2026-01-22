package com.lofi.lofiapps.service.impl.user;

import com.lofi.lofiapps.model.dto.response.UserProfileResponse;
import com.lofi.lofiapps.model.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.StorageService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserProfileUseCase {
  private final UserRepository userRepository;
  private final StorageService storageService;

  @Value("${app.storage.bucket-name:lofi-bucket}")
  private String bucketName;

  public UserProfileResponse execute() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(principal instanceof UserPrincipal)) {
      throw new RuntimeException("Unauthenticated");
    }

    UUID userId = ((UserPrincipal) principal).getId();
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

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
                ? UserProfileResponse.BranchInfo.builder()
                    .id(user.getBranch().getId())
                    .name(user.getBranch().getName())
                    .build()
                : null)
        .biodata(
            user.getUserBiodata() != null
                ? UserProfileResponse.BiodataInfo.builder()
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
}
