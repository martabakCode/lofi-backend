package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.dto.request.CreateUserRequest;
import com.lofi.lofiapps.dto.request.UpdateProfileRequest;
import com.lofi.lofiapps.dto.request.UserCriteria;
import com.lofi.lofiapps.dto.response.ApiResponse;
import com.lofi.lofiapps.dto.response.EligibilityAnalysisResponse;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.dto.response.UserProfileResponse;
import com.lofi.lofiapps.dto.response.UserSummaryResponse;
import com.lofi.lofiapps.enums.RoleName;
import com.lofi.lofiapps.enums.UserStatus;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
      @RequestParam(required = false) UserStatus status,
      @RequestParam(required = false) RoleName roleName,
      @RequestParam(required = false) UUID branchId,
      @PageableDefault(size = 10) Pageable pageable) {

    UserCriteria criteria =
        UserCriteria.builder().status(status).roleName(roleName).branchId(branchId).build();
    return ResponseEntity.ok(ApiResponse.success(userService.getUsers(criteria, pageable)));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Create a new user")
  public ResponseEntity<ApiResponse<UserSummaryResponse>> createUser(
      @Valid @RequestBody CreateUserRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(userService.createUser(request), "User created successfully"));
  }

  @GetMapping("/me")
  @Operation(summary = "Get my profile")
  public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
    return ResponseEntity.ok(ApiResponse.success(userService.getMyProfile()));
  }

  @PutMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update my profile")
  public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
      @RequestHeader(value = "User-Agent", defaultValue = "Unknown") String userAgent,
      @Valid @RequestBody UpdateProfileRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            userService.updateProfile(request, userAgent), "Profile updated successfully"));
  }

  @PutMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Update my profile photo")
  public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfilePhoto(
      @RequestPart(value = "photo") MultipartFile photo) {
    return ResponseEntity.ok(
        ApiResponse.success(
            userService.updateProfilePicture(photo), "Profile photo updated successfully"));
  }

  @GetMapping(value = "/me/photo", produces = MediaType.IMAGE_JPEG_VALUE)
  @Operation(summary = "Get my profile photo")
  public ResponseEntity<byte[]> getMyProfilePhoto() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(principal instanceof UserPrincipal)) {
      return ResponseEntity.status(401).build();
    }
    UUID userId = ((UserPrincipal) principal).getId();
    return ResponseEntity.ok(userService.getProfilePhoto(userId));
  }

  @GetMapping(value = "/{userId}/photo", produces = MediaType.IMAGE_JPEG_VALUE)
  @Operation(summary = "Get user profile photo by ID")
  public ResponseEntity<byte[]> getUserProfilePhoto(@PathVariable UUID userId) {
    return ResponseEntity.ok(userService.getProfilePhoto(userId));
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
  public ResponseEntity<ApiResponse<EligibilityAnalysisResponse>> checkEligibility(
      @PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(userService.analyzeEligibility(id)));
  }
}
