package com.lofi.lofiapps.service.impl.usecase.user;

import com.lofi.lofiapps.dto.request.UpdateProfileRequest;
import com.lofi.lofiapps.dto.response.UserProfileResponse;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.entity.UserBiodata;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateProfileUseCase {

  private final UserRepository userRepository;
  private final GetUserProfileUseCase getUserProfileUseCase;

  @Transactional
  public UserProfileResponse execute(UUID userId, UpdateProfileRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

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

    return getUserProfileUseCase.mapToProfileResponse(user);
  }
}
