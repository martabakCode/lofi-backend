package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.dto.request.CreateUserRequest;
import com.lofi.lofiapps.dto.request.UpdateProfileRequest;
import com.lofi.lofiapps.dto.request.UserCriteria;
import com.lofi.lofiapps.dto.response.EligibilityAnalysisResponse;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.dto.response.UserProfileResponse;
import com.lofi.lofiapps.dto.response.UserSummaryResponse;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.UserService;
import com.lofi.lofiapps.service.impl.usecase.user.CreateUserUseCase;
import com.lofi.lofiapps.service.impl.usecase.user.DeleteUserUseCase;
import com.lofi.lofiapps.service.impl.usecase.user.GetProfilePhotoUseCase;
import com.lofi.lofiapps.service.impl.usecase.user.GetUserProfileUseCase;
import com.lofi.lofiapps.service.impl.usecase.user.GetUsersUseCase;
import com.lofi.lofiapps.service.impl.usecase.user.UpdateProfileUseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final CreateUserUseCase createUserUseCase;
  private final DeleteUserUseCase deleteUserUseCase;
  private final GetUserProfileUseCase getUserProfileUseCase;
  private final GetUsersUseCase getUsersUseCase;
  private final UpdateProfileUseCase updateProfileUseCase;
  private final GetProfilePhotoUseCase getProfilePhotoUseCase;
  private final com.lofi.lofiapps.service.impl.usecase.user.UpdateProfilePictureUseCase
      updateProfilePictureUseCase;

  @Override
  public UserSummaryResponse createUser(CreateUserRequest request) {
    return createUserUseCase.execute(request);
  }

  @Override
  public void deleteUser(UUID userId) {
    deleteUserUseCase.execute(userId);
  }

  @Override
  public UserProfileResponse getUserProfile(UUID userId) {
    return getUserProfileUseCase.execute(userId);
  }

  @Override
  public UserProfileResponse getMyProfile() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(principal instanceof UserPrincipal)) {
      throw new RuntimeException("Unauthenticated");
    }

    UUID userId = ((UserPrincipal) principal).getId();
    return getUserProfileUseCase.execute(userId);
  }

  @Override
  public PagedResponse<UserSummaryResponse> getUsers(UserCriteria criteria, Pageable pageable) {
    return getUsersUseCase.execute(criteria, pageable);
  }

  @Override
  public UserProfileResponse updateProfile(UpdateProfileRequest request, String userAgent) {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(principal instanceof UserPrincipal)) {
      throw new RuntimeException("Unauthenticated");
    }

    UUID userId = ((UserPrincipal) principal).getId();
    return updateProfileUseCase.execute(userId, request, userAgent);
  }

  @Override
  public UserProfileResponse updateProfilePicture(
      org.springframework.web.multipart.MultipartFile photo) {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(principal instanceof UserPrincipal)) {
      throw new RuntimeException("Unauthenticated");
    }

    UUID userId = ((UserPrincipal) principal).getId();
    return updateProfilePictureUseCase.execute(userId, photo);
  }

  @Override
  public byte[] getProfilePhoto(UUID userId) {
    return getProfilePhotoUseCase.execute(userId);
  }

  @Override
  public EligibilityAnalysisResponse analyzeEligibility(UUID userId) {
    // Logic for this was null/commented out in original implementation
    return null;
  }
}
