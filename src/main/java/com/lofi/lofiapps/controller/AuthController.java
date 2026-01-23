package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.model.dto.request.*;
import com.lofi.lofiapps.model.dto.response.*;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.impl.auth.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(
    name = "Auth",
    description = "Authentication and Account Management")
public class AuthController {
  private final LoginUseCase loginUseCase;
  private final LogoutUseCase logoutUseCase;
  private final GoogleLoginUseCase googleLoginUseCase;
  private final ForgotPasswordUseCase forgotPasswordUseCase;
  private final ResetPasswordUseCase resetPasswordUseCase;
  private final ChangePasswordUseCase changePasswordUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;

  @PostMapping("/login")
  @io.swagger.v3.oas.annotations.Operation(summary = "Login with email and password")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest request) {
    System.out.println("Login attempt for email: " + request.getEmail());
    return ResponseEntity.ok(ApiResponse.success(loginUseCase.execute(request), "Login success"));
  }

  @PostMapping("/google")
  @io.swagger.v3.oas.annotations.Operation(summary = "Login with Google ID Token")
  public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(
      @Valid @RequestBody com.lofi.lofiapps.model.dto.request.GoogleLoginRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(googleLoginUseCase.execute(request), "Login success"));
  }

  @PostMapping("/refresh")
  @io.swagger.v3.oas.annotations.Operation(summary = "Refresh access token")
  public ResponseEntity<ApiResponse<LoginResponse>> refresh(
      @Valid @RequestBody RefreshTokenRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            refreshTokenUseCase.execute(request.getRefreshToken()), "Token refreshed"));
  }

  @PostMapping("/logout")
  @io.swagger.v3.oas.annotations.Operation(summary = "Logout current session")
  public ResponseEntity<ApiResponse<Object>> logout(@RequestHeader("Authorization") String token) {
    if (token != null && token.startsWith("Bearer ")) {
      logoutUseCase.execute(token.substring(7));
    }
    return ResponseEntity.ok(ApiResponse.success(null, "Logout success"));
  }

  @PostMapping("/forgot-password")
  @io.swagger.v3.oas.annotations.Operation(summary = "Request password reset")
  public ResponseEntity<ApiResponse<Object>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    forgotPasswordUseCase.execute(request);
    return ResponseEntity.ok(ApiResponse.success(null, "If email exists, reset link will be sent"));
  }

  @PostMapping("/reset-password")
  @io.swagger.v3.oas.annotations.Operation(summary = "Reset password with token")
  public ResponseEntity<ApiResponse<Object>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    resetPasswordUseCase.execute(request);
    return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
  }

  @PostMapping("/change-password")
  @io.swagger.v3.oas.annotations.Operation(summary = "Change password")
  public ResponseEntity<ApiResponse<Object>> changePassword(
      @Valid @RequestBody ChangePasswordRequest request) {
    changePasswordUseCase.execute(request);
    return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
  }

  @GetMapping("/me")
  @io.swagger.v3.oas.annotations.Operation(summary = "Get current user profile")
  public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
    UserPrincipal userPrincipal =
        (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    UserInfoResponse response =
        UserInfoResponse.builder()
            .id(userPrincipal.getId())
            .email(userPrincipal.getEmail())
            .username(userPrincipal.getUsername())
            .branchId(userPrincipal.getBranchId())
            .branchName(userPrincipal.getBranchName())
            .plafond(userPrincipal.getPlafond())
            .roles(userPrincipal.getRoles())
            .permissions(userPrincipal.getPermissions())
            .build();

    return ResponseEntity.ok(ApiResponse.success(response, "User info retrieved successfully"));
  }
}
