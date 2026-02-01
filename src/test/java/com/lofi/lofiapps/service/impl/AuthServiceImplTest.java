package com.lofi.lofiapps.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.request.*;
import com.lofi.lofiapps.dto.response.LoginResponse;
import com.lofi.lofiapps.service.impl.usecase.auth.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock private LoginUseCase loginUseCase;

  @Mock private LogoutUseCase logoutUseCase;

  @Mock private GoogleLoginUseCase googleLoginUseCase;

  @Mock private ForgotPasswordUseCase forgotPasswordUseCase;

  @Mock private ResetPasswordUseCase resetPasswordUseCase;

  @Mock private ChangePasswordUseCase changePasswordUseCase;

  @Mock private RefreshTokenUseCase refreshTokenUseCase;

  @Mock private RegisterUseCase registerUseCase;

  @InjectMocks private AuthServiceImpl authService;

  @Test
  @DisplayName("Login should delegate to LoginUseCase")
  void login_ShouldDelegateToUseCase() {
    // Arrange
    LoginRequest request = new LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("password123");

    LoginResponse expectedResponse =
        LoginResponse.builder()
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .expiresIn(3600)
            .tokenType("Bearer")
            .build();

    when(loginUseCase.execute(any(LoginRequest.class))).thenReturn(expectedResponse);

    // Act
    LoginResponse result = authService.login(request);

    // Assert
    assertNotNull(result);
    assertEquals("access-token", result.getAccessToken());
    assertEquals("refresh-token", result.getRefreshToken());
    assertEquals("Bearer", result.getTokenType());
    verify(loginUseCase, times(1)).execute(request);
  }

  @Test
  @DisplayName("Register should delegate to RegisterUseCase")
  void register_ShouldDelegateToUseCase() {
    // Arrange
    RegisterRequest request =
        RegisterRequest.builder()
            .email("newuser@example.com")
            .password("password123")
            .fullName("New User")
            .username("newuser")
            .phoneNumber("+6281234567890")
            .build();

    LoginResponse expectedResponse =
        LoginResponse.builder()
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .expiresIn(3600)
            .tokenType("Bearer")
            .build();

    when(registerUseCase.execute(any(RegisterRequest.class))).thenReturn(expectedResponse);

    // Act
    LoginResponse result = authService.register(request);

    // Assert
    assertNotNull(result);
    assertEquals("access-token", result.getAccessToken());
    verify(registerUseCase, times(1)).execute(request);
  }

  @Test
  @DisplayName("GoogleLogin should delegate to GoogleLoginUseCase")
  void googleLogin_ShouldDelegateToUseCase() {
    // Arrange
    GoogleLoginRequest request = new GoogleLoginRequest();
    request.setIdToken("google-id-token");

    LoginResponse expectedResponse =
        LoginResponse.builder()
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .expiresIn(3600)
            .tokenType("Bearer")
            .build();

    when(googleLoginUseCase.execute(any(GoogleLoginRequest.class))).thenReturn(expectedResponse);

    // Act
    LoginResponse result = authService.googleLogin(request);

    // Assert
    assertNotNull(result);
    assertEquals("access-token", result.getAccessToken());
    verify(googleLoginUseCase, times(1)).execute(request);
  }

  @Test
  @DisplayName("Refresh should delegate to RefreshTokenUseCase")
  void refresh_ShouldDelegateToUseCase() {
    // Arrange
    String refreshToken = "valid-refresh-token";
    LoginResponse expectedResponse =
        LoginResponse.builder()
            .accessToken("new-access-token")
            .refreshToken("new-refresh-token")
            .expiresIn(3600)
            .tokenType("Bearer")
            .build();

    when(refreshTokenUseCase.execute(refreshToken)).thenReturn(expectedResponse);

    // Act
    LoginResponse result = authService.refresh(refreshToken);

    // Assert
    assertNotNull(result);
    assertEquals("new-access-token", result.getAccessToken());
    verify(refreshTokenUseCase, times(1)).execute(refreshToken);
  }

  @Test
  @DisplayName("Logout should delegate to LogoutUseCase")
  void logout_ShouldDelegateToUseCase() {
    // Arrange
    String token = "valid-token";
    doNothing().when(logoutUseCase).execute(token);

    // Act
    authService.logout(token);

    // Assert
    verify(logoutUseCase, times(1)).execute(token);
  }

  @Test
  @DisplayName("ForgotPassword should delegate to ForgotPasswordUseCase")
  void forgotPassword_ShouldDelegateToUseCase() {
    // Arrange
    ForgotPasswordRequest request = new ForgotPasswordRequest();
    request.setEmail("test@example.com");
    doNothing().when(forgotPasswordUseCase).execute(any(ForgotPasswordRequest.class));

    // Act
    authService.forgotPassword(request);

    // Assert
    verify(forgotPasswordUseCase, times(1)).execute(request);
  }

  @Test
  @DisplayName("ResetPassword should delegate to ResetPasswordUseCase")
  void resetPassword_ShouldDelegateToUseCase() {
    // Arrange
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setToken("reset-token");
    request.setNewPassword("newpassword123");
    doNothing().when(resetPasswordUseCase).execute(any(ResetPasswordRequest.class));

    // Act
    authService.resetPassword(request);

    // Assert
    verify(resetPasswordUseCase, times(1)).execute(request);
  }

  @Test
  @DisplayName("ChangePassword should delegate to ChangePasswordUseCase")
  void changePassword_ShouldDelegateToUseCase() {
    // Arrange
    ChangePasswordRequest request = new ChangePasswordRequest();
    request.setOldPassword("oldpassword");
    request.setNewPassword("newpassword123");
    doNothing().when(changePasswordUseCase).execute(any(ChangePasswordRequest.class));

    // Act
    authService.changePassword(request);

    // Assert
    verify(changePasswordUseCase, times(1)).execute(request);
  }
}
