package com.lofi.lofiapps.service.impl.usecase.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.response.LoginResponse;
import com.lofi.lofiapps.entity.RefreshToken;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.RoleName;
import com.lofi.lofiapps.enums.UserStatus;
import com.lofi.lofiapps.repository.RefreshTokenRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.security.jwt.JwtUtils;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

  @Mock private JwtUtils jwtUtils;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private RefreshTokenUseCase refreshTokenUseCase;

  private String validRefreshToken;
  private UUID userId;
  private User testUser;
  private RefreshToken refreshTokenEntity;

  @BeforeEach
  void setUp() {
    validRefreshToken = "valid.refresh.token";
    userId = UUID.randomUUID();

    Role customerRole = Role.builder().id(UUID.randomUUID()).name(RoleName.ROLE_CUSTOMER).build();

    testUser =
        User.builder()
            .id(userId)
            .email("test@example.com")
            .username("testuser")
            .fullName("Test User")
            .status(UserStatus.ACTIVE)
            .roles(Collections.singleton(customerRole))
            .build();

    refreshTokenEntity =
        RefreshToken.builder()
            .id(UUID.randomUUID())
            .token(validRefreshToken)
            .user(testUser)
            .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
            .revoked(false)
            .build();
  }

  @Test
  @DisplayName("Execute should return new access token with valid refresh token")
  void execute_ShouldReturnNewAccessToken_WithValidRefreshToken() {
    // Arrange
    when(jwtUtils.validateJwtToken(validRefreshToken)).thenReturn(true);
    when(refreshTokenRepository.findByToken(validRefreshToken))
        .thenReturn(Optional.of(refreshTokenEntity));
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

    String newAccessToken = "new.access.token";
    when(jwtUtils.generateJwtToken(any())).thenReturn(newAccessToken);
    when(jwtUtils.getExpirationFromJwtToken(newAccessToken)).thenReturn(3600000L);

    // Act
    LoginResponse result = refreshTokenUseCase.execute(validRefreshToken);

    // Assert
    assertNotNull(result);
    assertEquals(newAccessToken, result.getAccessToken());
    assertEquals(validRefreshToken, result.getRefreshToken());
    assertEquals("Bearer", result.getTokenType());
    assertEquals(3600, result.getExpiresIn());
  }

  @Test
  @DisplayName("Execute should throw exception when token is null")
  void execute_ShouldThrowException_WhenTokenIsNull() {
    // Arrange
    when(jwtUtils.validateJwtToken(null)).thenReturn(false);

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> refreshTokenUseCase.execute(null));
    assertEquals("Invalid Refresh Token", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when token is invalid")
  void execute_ShouldThrowException_WhenTokenIsInvalid() {
    // Arrange
    String invalidToken = "invalid.token";
    when(jwtUtils.validateJwtToken(invalidToken)).thenReturn(false);

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> refreshTokenUseCase.execute(invalidToken));
    assertEquals("Invalid Refresh Token", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when token not found in database")
  void execute_ShouldThrowException_WhenTokenNotFoundInDatabase() {
    // Arrange
    when(jwtUtils.validateJwtToken(validRefreshToken)).thenReturn(true);
    when(refreshTokenRepository.findByToken(validRefreshToken)).thenReturn(Optional.empty());

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> refreshTokenUseCase.execute(validRefreshToken));
    assertEquals("Refresh Token not found in DB", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when token is revoked")
  void execute_ShouldThrowException_WhenTokenIsRevoked() {
    // Arrange
    RefreshToken revokedToken =
        RefreshToken.builder()
            .id(UUID.randomUUID())
            .token(validRefreshToken)
            .user(testUser)
            .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
            .revoked(true)
            .build();

    when(jwtUtils.validateJwtToken(validRefreshToken)).thenReturn(true);
    when(refreshTokenRepository.findByToken(validRefreshToken))
        .thenReturn(Optional.of(revokedToken));

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> refreshTokenUseCase.execute(validRefreshToken));
    assertEquals("Refresh Token Revoked (AUTH_REFRESH_TOKEN_REVOKED)", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when token is expired")
  void execute_ShouldThrowException_WhenTokenIsExpired() {
    // Arrange
    RefreshToken expiredToken =
        RefreshToken.builder()
            .id(UUID.randomUUID())
            .token(validRefreshToken)
            .user(testUser)
            .expiryDate(Instant.now().minus(1, ChronoUnit.DAYS))
            .revoked(false)
            .build();

    when(jwtUtils.validateJwtToken(validRefreshToken)).thenReturn(true);
    when(refreshTokenRepository.findByToken(validRefreshToken))
        .thenReturn(Optional.of(expiredToken));

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> refreshTokenUseCase.execute(validRefreshToken));
    assertEquals("Refresh Token Expired (AUTH_REFRESH_TOKEN_EXPIRED)", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should throw exception when user not found")
  void execute_ShouldThrowException_WhenUserNotFound() {
    // Arrange
    when(jwtUtils.validateJwtToken(validRefreshToken)).thenReturn(true);
    when(refreshTokenRepository.findByToken(validRefreshToken))
        .thenReturn(Optional.of(refreshTokenEntity));
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> refreshTokenUseCase.execute(validRefreshToken));
    assertEquals("User not found", exception.getMessage());
  }
}
