package com.lofi.lofiapps.service.impl.usecase.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.security.jwt.JwtUtils;
import com.lofi.lofiapps.security.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

  @Mock private TokenBlacklistService tokenBlacklistService;
  @Mock private JwtUtils jwtUtils;

  @InjectMocks private LogoutUseCase logoutUseCase;

  private String validToken;

  @BeforeEach
  void setUp() {
    validToken = "valid.jwt.token";
  }

  @Test
  @DisplayName("Execute should blacklist valid token")
  void execute_ShouldBlacklistValidToken() {
    // Arrange
    when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
    when(jwtUtils.getExpirationFromJwtToken(validToken)).thenReturn(3600000L);
    doNothing().when(tokenBlacklistService).blacklistToken(validToken, 3600000L);

    // Act
    logoutUseCase.execute(validToken);

    // Assert
    verify(jwtUtils).validateJwtToken(validToken);
    verify(jwtUtils).getExpirationFromJwtToken(validToken);
    verify(tokenBlacklistService).blacklistToken(validToken, 3600000L);
  }

  @Test
  @DisplayName("Execute should not blacklist invalid token")
  void execute_ShouldNotBlacklistInvalidToken() {
    // Arrange
    String invalidToken = "invalid.token";
    when(jwtUtils.validateJwtToken(invalidToken)).thenReturn(false);

    // Act
    logoutUseCase.execute(invalidToken);

    // Assert
    verify(jwtUtils).validateJwtToken(invalidToken);
    verify(jwtUtils, never()).getExpirationFromJwtToken(anyString());
    verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
  }

  @Test
  @DisplayName("Execute should handle null token gracefully")
  void execute_ShouldHandleNullToken() {
    // Arrange
    // Act & Assert
    assertDoesNotThrow(() -> logoutUseCase.execute(null));
    verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
  }

  @Test
  @DisplayName("Execute should handle empty token gracefully")
  void execute_ShouldHandleEmptyToken() {
    // Arrange
    String emptyToken = "";
    when(jwtUtils.validateJwtToken(emptyToken)).thenReturn(false);

    // Act & Assert
    assertDoesNotThrow(() -> logoutUseCase.execute(emptyToken));
    verify(jwtUtils).validateJwtToken(emptyToken);
    verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
  }

  @Test
  @DisplayName("Execute should use correct expiration time")
  void execute_ShouldUseCorrectExpirationTime() {
    // Arrange
    long expectedExpiration = 7200000L; // 2 hours
    when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
    when(jwtUtils.getExpirationFromJwtToken(validToken)).thenReturn(expectedExpiration);
    doNothing().when(tokenBlacklistService).blacklistToken(validToken, expectedExpiration);

    // Act
    logoutUseCase.execute(validToken);

    // Assert
    verify(tokenBlacklistService).blacklistToken(validToken, expectedExpiration);
  }
}
