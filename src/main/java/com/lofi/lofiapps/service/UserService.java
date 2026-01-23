package com.lofi.lofiapps.service;

import com.lofi.lofiapps.model.dto.request.CreateUserRequest;
import com.lofi.lofiapps.model.dto.request.UpdateProfileRequest;
import com.lofi.lofiapps.model.dto.response.*;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface UserService {
  UserSummaryResponse createUser(CreateUserRequest request);

  void deleteUser(UUID userId);

  UserProfileResponse getUserProfile(UUID userId); // Overload if needed

  UserProfileResponse getMyProfile(); // Implicitly gets current user

  PagedResponse<UserSummaryResponse> getUsers(Pageable pageable);

  UserProfileResponse updateProfile(UpdateProfileRequest request);

  // AI / Analysis
  EligibilityAnalysisResponse analyzeEligibility(UUID userId);
}
