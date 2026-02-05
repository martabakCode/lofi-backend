package com.lofi.lofiapps.service.impl.usecase.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.request.LoginRequest;
import com.lofi.lofiapps.dto.response.LoginResponse;
import com.lofi.lofiapps.entity.RefreshToken;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.repository.RefreshTokenRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.security.jwt.JwtUtils;
import com.lofi.lofiapps.security.service.UserPrincipal;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

  @Mock private AuthenticationManager authenticationManager;

  @Mock private JwtUtils jwtUtils;

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private LoginUseCase loginUseCase;

  private UUID testUserId;
  private UserPrincipal testUserPrincipal;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testUserPrincipal =
        new UserPrincipal(
            testUserId,
            "test@example.com",
            "password",
            UUID.randomUUID(),
            "Test Branch",
            BigDecimal.valueOf(10000000),
            com.lofi.lofiapps.enums.UserStatus.ACTIVE,
            Collections.emptyList());
  }

  @Test
  @DisplayName("Execute should return LoginResponse with valid credentials")
  void execute_ShouldReturnLoginResponse_WhenCredentialsAreValid() {
    // Arrange
    LoginRequest request = new LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("password123");

    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUserPrincipal);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);

    when(jwtUtils.generateJwtToken(authentication)).thenReturn("access-token");
    when(jwtUtils.getExpirationFromJwtToken("access-token")).thenReturn(3600000L);
    when(jwtUtils.generateRefreshToken(authentication)).thenReturn("refresh-token");
    when(jwtUtils.getExpirationFromJwtToken("refresh-token")).thenReturn(604800000L);

    doNothing().when(refreshTokenRepository).deleteByUserId(testUserId);
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Mock user retrieval for pin/profile status
    User user = User.builder().id(testUserId).pinSet(true).profileCompleted(true).build();
    when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));

    // Act
    LoginResponse result = loginUseCase.execute(request);

    // Assert
    assertNotNull(result);
    assertEquals("access-token", result.getAccessToken());
    assertEquals("refresh-token", result.getRefreshToken());
    assertEquals("Bearer", result.getTokenType());
    assertEquals(3600, result.getExpiresIn());
    assertTrue(result.getPinSet());
    assertTrue(result.getProfileCompleted());

    verify(authenticationManager, times(1))
        .authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(jwtUtils, times(1)).generateJwtToken(authentication);
    verify(jwtUtils, times(1)).generateRefreshToken(authentication);
    verify(refreshTokenRepository, times(1)).deleteByUserId(testUserId);
    verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    verify(userRepository, times(1)).findById(testUserId);
  }

  @Test
  @DisplayName("Execute should update FCM token when provided")
  void execute_ShouldUpdateFcmToken_WhenProvided() {
    // Arrange
    LoginRequest request = new LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("password123");
    request.setFcmToken("fcm-token-123");

    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUserPrincipal);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);

    when(jwtUtils.generateJwtToken(authentication)).thenReturn("access-token");
    when(jwtUtils.getExpirationFromJwtToken("access-token")).thenReturn(3600000L);
    when(jwtUtils.generateRefreshToken(authentication)).thenReturn("refresh-token");
    when(jwtUtils.getExpirationFromJwtToken("refresh-token")).thenReturn(604800000L);

    doNothing().when(refreshTokenRepository).deleteByUserId(testUserId);
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    User user = User.builder().id(testUserId).email("test@example.com").build();
    when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    LoginResponse result = loginUseCase.execute(request);

    // Assert
    assertNotNull(result);
    verify(userRepository, times(1)).findById(testUserId);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Execute should throw exception with invalid credentials")
  void execute_ShouldThrowException_WhenCredentialsAreInvalid() {
    // Arrange
    LoginRequest request = new LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("wrongpassword");

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    // Act & Assert
    assertThrows(BadCredentialsException.class, () -> loginUseCase.execute(request));

    verify(authenticationManager, times(1))
        .authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(jwtUtils, never()).generateJwtToken(any());
    verify(refreshTokenRepository, never()).save(any());
  }

  @Test
  @DisplayName("Execute should not update FCM token when not provided")
  void execute_ShouldNotUpdateFcmToken_WhenNotProvided() {
    // Arrange
    LoginRequest request = new LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("password123");
    // FCM token not set

    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUserPrincipal);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);

    when(jwtUtils.generateJwtToken(authentication)).thenReturn("access-token");
    when(jwtUtils.getExpirationFromJwtToken("access-token")).thenReturn(3600000L);
    when(jwtUtils.generateRefreshToken(authentication)).thenReturn("refresh-token");
    when(jwtUtils.getExpirationFromJwtToken("refresh-token")).thenReturn(604800000L);

    doNothing().when(refreshTokenRepository).deleteByUserId(testUserId);
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    LoginResponse result = loginUseCase.execute(request);

    // Assert
    assertNotNull(result);
    verify(userRepository, never()).findById(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Execute should revoke existing refresh tokens")
  void execute_ShouldRevokeExistingTokens() {
    // Arrange
    LoginRequest request = new LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("password123");

    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUserPrincipal);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);

    when(jwtUtils.generateJwtToken(authentication)).thenReturn("access-token");
    when(jwtUtils.getExpirationFromJwtToken("access-token")).thenReturn(3600000L);
    when(jwtUtils.generateRefreshToken(authentication)).thenReturn("refresh-token");
    when(jwtUtils.getExpirationFromJwtToken("refresh-token")).thenReturn(604800000L);

    doNothing().when(refreshTokenRepository).deleteByUserId(testUserId);
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    loginUseCase.execute(request);

    // Assert
    verify(refreshTokenRepository, times(1)).deleteByUserId(testUserId);
  }
}
