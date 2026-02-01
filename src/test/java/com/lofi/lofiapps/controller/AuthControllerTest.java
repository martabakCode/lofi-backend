package com.lofi.lofiapps.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lofi.lofiapps.dto.request.*;
import com.lofi.lofiapps.dto.response.LoginResponse;
import com.lofi.lofiapps.security.jwt.JwtUtils;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.AuthService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  private MockMvc mockMvc;

  @Mock private AuthService authService;

  @Mock private JwtUtils jwtUtils;

  @InjectMocks private AuthController authController;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("Login should return success with valid credentials")
  void login_ShouldReturnSuccess_WhenCredentialsAreValid() throws Exception {
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

    when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Login success"))
        .andExpect(jsonPath("$.data.accessToken").value("access-token"))
        .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
        .andExpect(jsonPath("$.data.tokenType").value("Bearer"));

    verify(authService, times(1)).login(any(LoginRequest.class));
  }

  @Test
  @DisplayName("Register should return success with valid data")
  void register_ShouldReturnSuccess_WhenDataIsValid() throws Exception {
    // Arrange
    RegisterRequest request = new RegisterRequest();
    request.setEmail("newuser@example.com");
    request.setPassword("password123");
    request.setFullName("New User");
    request.setUsername("newuser");
    request.setPhoneNumber("+6281234567890");

    LoginResponse expectedResponse =
        LoginResponse.builder()
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .expiresIn(3600)
            .tokenType("Bearer")
            .build();

    when(authService.register(any(RegisterRequest.class))).thenReturn(expectedResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Registration successful"));

    verify(authService, times(1)).register(any(RegisterRequest.class));
  }

  @Test
  @DisplayName("Refresh token should return new tokens")
  void refresh_ShouldReturnNewTokens_WhenRefreshTokenIsValid() throws Exception {
    // Arrange
    RefreshTokenRequest request = new RefreshTokenRequest();
    request.setRefreshToken("valid-refresh-token");

    LoginResponse expectedResponse =
        LoginResponse.builder()
            .accessToken("new-access-token")
            .refreshToken("new-refresh-token")
            .expiresIn(3600)
            .tokenType("Bearer")
            .build();

    when(authService.refresh("valid-refresh-token")).thenReturn(expectedResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));

    verify(authService, times(1)).refresh("valid-refresh-token");
  }

  @Test
  @DisplayName("Logout should return success")
  void logout_ShouldReturnSuccess_WhenTokenIsProvided() throws Exception {
    // Arrange
    doNothing().when(authService).logout(anyString());

    // Act & Assert
    mockMvc
        .perform(post("/auth/logout").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Logout success"));

    verify(authService, times(1)).logout("valid-token");
  }

  @Test
  @DisplayName("Forgot password should return success")
  void forgotPassword_ShouldReturnSuccess() throws Exception {
    // Arrange
    ForgotPasswordRequest request = new ForgotPasswordRequest();
    request.setEmail("test@example.com");

    doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(authService, times(1)).forgotPassword(any(ForgotPasswordRequest.class));
  }

  @Test
  @DisplayName("Reset password should return success")
  void resetPassword_ShouldReturnSuccess() throws Exception {
    // Arrange
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setToken("reset-token");
    request.setNewPassword("newpassword123");

    doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(authService, times(1)).resetPassword(any(ResetPasswordRequest.class));
  }

  @Test
  @DisplayName("Change password should return success")
  void changePassword_ShouldReturnSuccess() throws Exception {
    // Arrange
    ChangePasswordRequest request = new ChangePasswordRequest();
    request.setOldPassword("oldpassword");
    request.setNewPassword("newpassword123");

    doNothing().when(authService).changePassword(any(ChangePasswordRequest.class));

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(authService, times(1)).changePassword(any(ChangePasswordRequest.class));
  }

  @Test
  @DisplayName("Get current user should return user info")
  void getCurrentUser_ShouldReturnUserInfo() throws Exception {
    // Arrange
    UUID userId = UUID.randomUUID();
    UUID branchId = UUID.randomUUID();
    UserPrincipal userPrincipal =
        new UserPrincipal(
            userId,
            "test@example.com",
            "password",
            branchId,
            "Test Branch",
            BigDecimal.valueOf(10000000),
            com.lofi.lofiapps.enums.UserStatus.ACTIVE,
            Collections.emptyList());

    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userPrincipal);
    SecurityContextHolder.setContext(securityContext);

    // Act & Assert
    mockMvc
        .perform(get("/auth/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").value("test@example.com"));

    // Cleanup
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Google login should return success")
  void googleLogin_ShouldReturnSuccess() throws Exception {
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

    when(authService.googleLogin(any(GoogleLoginRequest.class))).thenReturn(expectedResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(authService, times(1)).googleLogin(any(GoogleLoginRequest.class));
  }
}
