package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.dto.request.*;
import com.lofi.lofiapps.dto.response.*;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication and Account Management")
public class AuthController {
  private final AuthService authService;

  @PostMapping("/login")
  @Operation(summary = "Login with email and password")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest request) {
    log.info("Login attempt for email: {}", request.getEmail());
    return ResponseEntity.ok(ApiResponse.success(authService.login(request), "Login success"));
  }

  @PostMapping("/pin-login")
  @Operation(summary = "Login with PIN")
  public ResponseEntity<ApiResponse<LoginResponse>> pinLogin(
      @Valid @RequestBody PinLoginRequest request) {
    return ResponseEntity.ok(ApiResponse.success(authService.pinLogin(request), "Login success"));
  }

  @PostMapping("/pin-reset")
  @Operation(summary = "Reset PIN via email")
  public ResponseEntity<ApiResponse<Object>> pinReset(@Valid @RequestBody PinResetRequest request) {
    authService.pinReset(request);
    return ResponseEntity.ok(ApiResponse.success(null, "If email exists, new PIN will be sent"));
  }

  @PostMapping("/register")
  @Operation(summary = "Register a new customer")
  public ResponseEntity<ApiResponse<LoginResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(authService.register(request), "Registration successful"));
  }

  @PostMapping("/google")
  @Operation(summary = "Login with Google ID Token")
  public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(
      @Valid @RequestBody GoogleLoginRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(authService.googleLogin(request), "Login success"));
  }

  @PostMapping("/refresh")
  @Operation(summary = "Refresh access token")
  public ResponseEntity<ApiResponse<LoginResponse>> refresh(
      @Valid @RequestBody RefreshTokenRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(authService.refresh(request.getRefreshToken()), "Token refreshed"));
  }

  @PostMapping("/logout")
  @Operation(summary = "Logout current session")
  public ResponseEntity<ApiResponse<Object>> logout(@RequestHeader("Authorization") String token) {
    if (token != null && token.startsWith("Bearer ")) {
      authService.logout(token.substring(7));
    }
    return ResponseEntity.ok(ApiResponse.success(null, "Logout success"));
  }

  @PostMapping("/forgot-password")
  @Operation(summary = "Request password reset")
  public ResponseEntity<ApiResponse<Object>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    authService.forgotPassword(request);
    return ResponseEntity.ok(ApiResponse.success(null, "If email exists, reset link will be sent"));
  }

  @PostMapping("/reset-password")
  @Operation(summary = "Reset password with token")
  public ResponseEntity<ApiResponse<Object>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request);
    return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
  }

  @PostMapping("/change-password")
  @Operation(summary = "Change password")
  public ResponseEntity<ApiResponse<Object>> changePassword(
      @Valid @RequestBody ChangePasswordRequest request) {
    authService.changePassword(request);
    return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
  }

  @GetMapping("/me")
  @Operation(summary = "Get current user profile")
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
