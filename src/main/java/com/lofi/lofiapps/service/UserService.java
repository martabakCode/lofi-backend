package com.lofi.lofiapps.service;

import com.lofi.lofiapps.dto.request.CreateUserRequest;
import com.lofi.lofiapps.dto.request.UpdateProfileRequest;
import com.lofi.lofiapps.dto.response.*;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface UserService {
  UserSummaryResponse createUser(CreateUserRequest request);

  void deleteUser(UUID userId);

  UserProfileResponse getUserProfile(UUID userId); // Overload if needed

  UserProfileResponse getMyProfile(); // Implicitly gets current user

  PagedResponse<UserSummaryResponse> getUsers(
      com.lofi.lofiapps.dto.request.UserCriteria criteria, Pageable pageable);

  UserProfileResponse updateProfile(UpdateProfileRequest request, String userAgent);

  UserProfileResponse updateProfilePicture(org.springframework.web.multipart.MultipartFile photo);

  byte[] getProfilePhoto(UUID userId);

  // AI / Analysis
  EligibilityAnalysisResponse analyzeEligibility(UUID userId);
}
