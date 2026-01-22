package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.model.dto.request.UpdateProfileRequest;
import com.lofi.lofiapps.model.dto.request.UserCriteria;
import com.lofi.lofiapps.model.dto.response.*;
import com.lofi.lofiapps.model.dto.response.PagedResponse;
import com.lofi.lofiapps.model.dto.response.UserProfileResponse;
import com.lofi.lofiapps.model.dto.response.UserSummaryResponse;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.impl.admin.AdminForceLogoutUseCase;
import com.lofi.lofiapps.service.impl.user.CreateUserUseCase;
import com.lofi.lofiapps.service.impl.user.DeleteUserUseCase;
import com.lofi.lofiapps.service.impl.user.GetUserProfileUseCase;
import com.lofi.lofiapps.service.impl.user.GetUsersUseCase;
import com.lofi.lofiapps.service.impl.user.UpdateUserProfileUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
  private final GetUserProfileUseCase getUserProfileUseCase;
  private final UpdateUserProfileUseCase updateUserProfileUseCase;
  private final GetUsersUseCase getUsersUseCase;
  private final CreateUserUseCase createUserUseCase;
  private final AdminForceLogoutUseCase adminForceLogoutUseCase;
  private final DeleteUserUseCase deleteUserUseCase;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<PagedResponse<UserSummaryResponse>>> getUsers(
      @RequestParam(required = false) com.lofi.lofiapps.model.enums.UserStatus status,
      @RequestParam(required = false) com.lofi.lofiapps.model.enums.RoleName roleName,
      @RequestParam(required = false) java.util.UUID branchId,
      @PageableDefault(size = 10) Pageable pageable) {

    UserCriteria criteria =
        UserCriteria.builder().status(status).roleName(roleName).branchId(branchId).build();

    return ResponseEntity.ok(ApiResponse.success(getUsersUseCase.execute(criteria, pageable)));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<UserSummaryResponse>> createUser(
      @Valid @RequestBody com.lofi.lofiapps.model.dto.request.CreateUserRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(createUserUseCase.execute(request), "User created successfully"));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
    return ResponseEntity.ok(ApiResponse.success(getUserProfileUseCase.execute()));
  }

  @PutMapping("/me")
  public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
      @Valid @RequestBody UpdateProfileRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            updateUserProfileUseCase.execute(request), "Profile updated successfully"));
  }

  @PostMapping("/admin/users/{userId}/force-logout")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<Void>> forceLogout(@PathVariable UUID userId) {
    adminForceLogoutUseCase.execute(userId);
    return ResponseEntity.ok(ApiResponse.success(null, "User forced logout successfully"));
  }

  @DeleteMapping("/me")
  public ResponseEntity<ApiResponse<Void>> deleteAccount() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(principal instanceof UserPrincipal)) {
      return ResponseEntity.status(401).build();
    }
    UUID userId = ((UserPrincipal) principal).getId();
    deleteUserUseCase.execute(userId);
    return ResponseEntity.ok(ApiResponse.success(null, "Account deleted successfully"));
  }

  @DeleteMapping("/admin/users/{userId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {
    deleteUserUseCase.execute(userId);
    return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
  }
}
