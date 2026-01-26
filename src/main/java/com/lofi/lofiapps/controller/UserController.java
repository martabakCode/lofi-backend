package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.dto.request.UpdateProfileRequest;
import com.lofi.lofiapps.dto.request.UserCriteria;
import com.lofi.lofiapps.dto.response.ApiResponse;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.dto.response.UserProfileResponse;
import com.lofi.lofiapps.dto.response.UserSummaryResponse;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.impl.AdminServiceImpl;
import com.lofi.lofiapps.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User", description = "User Management")
public class UserController {
  private final AdminServiceImpl adminService;
  private final UserServiceImpl userService;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Get all users")
  public ResponseEntity<ApiResponse<PagedResponse<UserSummaryResponse>>> getUsers(
      @RequestParam(required = false) com.lofi.lofiapps.enums.UserStatus status,
      @RequestParam(required = false) com.lofi.lofiapps.enums.RoleName roleName,
      @RequestParam(required = false) java.util.UUID branchId,
      @PageableDefault(size = 10) Pageable pageable) {

    UserCriteria criteria =
        UserCriteria.builder().status(status).roleName(roleName).branchId(branchId).build();
    return ResponseEntity.ok(ApiResponse.success(userService.getUsers(criteria, pageable)));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Create a new user")
  public ResponseEntity<ApiResponse<UserSummaryResponse>> createUser(
      @Valid @RequestBody com.lofi.lofiapps.dto.request.CreateUserRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(userService.createUser(request), "User created successfully"));
  }

  @GetMapping("/me")
  @Operation(summary = "Get my profile")
  public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
    return ResponseEntity.ok(ApiResponse.success(userService.getMyProfile()));
  }

  @PutMapping("/me")
  @Operation(summary = "Update my profile")
  public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
      @Valid @RequestBody UpdateProfileRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(userService.updateProfile(request), "Profile updated successfully"));
  }

  @PostMapping("/admin/users/{userId}/force-logout")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Force logout a user")
  public ResponseEntity<ApiResponse<Void>> forceLogout(@PathVariable UUID userId) {
    adminService.forceLogoutUser(userId);
    return ResponseEntity.ok(ApiResponse.success(null, "User forced logout successfully"));
  }

  @DeleteMapping("/me")
  @Operation(summary = "Delete my account")
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
  @Operation(summary = "Delete a user")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {
    userService.deleteUser(userId);
    return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
  }

  @GetMapping("/{id}/eligibility")
  @PreAuthorize("hasRole('ADMIN') or hasRole('MARKETING') or hasRole('BRANCH_MANAGER')")
  @Operation(summary = "Check User Eligibility via AI")
  public ResponseEntity<ApiResponse<com.lofi.lofiapps.dto.response.EligibilityAnalysisResponse>>
      checkEligibility(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(userService.analyzeEligibility(id)));
  }
}
