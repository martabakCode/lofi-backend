package com.lofi.lofiapps.service.impl.usecase.user;

import com.lofi.lofiapps.dto.request.UpdateProfileRequest;
import com.lofi.lofiapps.dto.response.UserProfileResponse;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.entity.UserBiodata;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.StorageService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateProfileUseCase {

  private final UserRepository userRepository;
  private final GetUserProfileUseCase getUserProfileUseCase;
  private final StorageService storageService;
  private final com.lofi.lofiapps.repository.UserBiodataRepository userBiodataRepository;
  private final com.lofi.lofiapps.repository.ProductRepository productRepository;

  @org.springframework.beans.factory.annotation.Value("${app.storage.bucket-name:lofi-bucket}")
  private String bucketName;

  @Transactional
  public UserProfileResponse execute(UUID userId, UpdateProfileRequest request, String userAgent) {
    String currentUserId = userId.toString();
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

    user.setFullName(request.getFullName());
    user.setPhoneNumber(request.getPhoneNumber());

    UserBiodata biodata = user.getUserBiodata();
    if (biodata == null) {
      java.util.Optional<UserBiodata> existingBiodata = userBiodataRepository.findByUser(user);
      if (existingBiodata.isPresent()) {
        biodata = existingBiodata.get();
        user.setUserBiodata(biodata);
      } else {
        biodata = new UserBiodata();
        biodata.setUser(user);
        biodata.setCreatedBy(currentUserId);
        user.setUserBiodata(biodata);
      }
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

    // Update user location coordinates (nullable)
    user.setLongitude(request.getLongitude());
    user.setLatitude(request.getLatitude());

    user.setProfileCompleted(true);

    if (user.getProduct() == null) {
      productRepository.findTopByIsActiveTrueOrderByMinLoanAmountAsc().ifPresent(user::setProduct);
    }

    user.setLastModifiedBy(currentUserId + (userAgent != null ? " (" + userAgent + ")" : ""));
    biodata.setLastModifiedBy(currentUserId + (userAgent != null ? " (" + userAgent + ")" : ""));

    user = userRepository.save(user);

    return getUserProfileUseCase.mapToProfileResponse(user);
  }
}
