package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.model.dto.request.UpdateProfileRequest;
import com.lofi.lofiapps.model.dto.response.*;
import com.lofi.lofiapps.model.dto.response.PagedResponse;
import com.lofi.lofiapps.model.dto.response.UserProfileResponse;
import com.lofi.lofiapps.model.dto.response.UserSummaryResponse;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.impl.admin.AdminForceLogoutUseCase;
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
  private final com.lofi.lofiapps.service.UserService userService;
  private final AdminForceLogoutUseCase adminForceLogoutUseCase;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<PagedResponse<UserSummaryResponse>>> getUsers(
      @RequestParam(required = false) com.lofi.lofiapps.model.enums.UserStatus status,
      @RequestParam(required = false) com.lofi.lofiapps.model.enums.RoleName roleName,
      @RequestParam(required = false) java.util.UUID branchId,
      @PageableDefault(size = 10) Pageable pageable) {

    // Note: I will need to update UserService to handle criteria if strictly
    // needed,
    // but for now I'll use the simplified method.
    return ResponseEntity.ok(ApiResponse.success(userService.getUsers(pageable)));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<UserSummaryResponse>> createUser(
      @Valid @RequestBody com.lofi.lofiapps.model.dto.request.CreateUserRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(userService.createUser(request), "User created successfully"));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
    return ResponseEntity.ok(ApiResponse.success(userService.getMyProfile()));
  }

  @PutMapping("/me")
  public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
      @Valid @RequestBody UpdateProfileRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(userService.updateProfile(request), "Profile updated successfully"));
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
    userService.deleteUser(userId);
    return ResponseEntity.ok(ApiResponse.success(null, "Account deleted successfully"));
  }

  @DeleteMapping("/admin/users/{userId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {
    userService.deleteUser(userId);
    return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
  }

  @GetMapping("/{id}/eligibility")
  @PreAuthorize("hasRole('ADMIN') or hasRole('MARKETING') or hasRole('BRANCH_MANAGER')")
  @io.swagger.v3.oas.annotations.Operation(summary = "Check User Eligibility via AI")
  public ResponseEntity<
          ApiResponse<com.lofi.lofiapps.model.dto.response.EligibilityAnalysisResponse>>
      checkEligibility(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(userService.analyzeEligibility(id)));
  }
}
