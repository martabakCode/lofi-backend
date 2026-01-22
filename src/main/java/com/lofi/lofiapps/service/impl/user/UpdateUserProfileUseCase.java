package com.lofi.lofiapps.service.impl.user;

import com.lofi.lofiapps.model.dto.request.UpdateProfileRequest;
import com.lofi.lofiapps.model.dto.response.UserProfileResponse;
import com.lofi.lofiapps.model.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.security.service.UserPrincipal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserProfileUseCase {
  private final UserRepository userRepository;

  @Transactional
  public UserProfileResponse execute(UpdateProfileRequest request) {
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
    com.lofi.lofiapps.model.entity.UserBiodata biodata = user.getUserBiodata();
    if (biodata == null) {
      biodata = new com.lofi.lofiapps.model.entity.UserBiodata();
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

    return UserProfileResponse.builder()
        .id(user.getId())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .phoneNumber(user.getPhoneNumber())
        .profilePictureUrl(user.getProfilePictureUrl())
        .branch(
            user.getBranch() != null
                ? UserProfileResponse.BranchInfo.builder()
                    .id(user.getBranch().getId())
                    .name(user.getBranch().getName())
                    .build()
                : null)
        .build();
  }
}
