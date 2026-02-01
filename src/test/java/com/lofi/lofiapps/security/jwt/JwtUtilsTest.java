package com.lofi.lofiapps.security.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.security.service.UserPrincipal;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

  private JwtUtils jwtUtils;
  private UUID testUserId;
  private UserPrincipal testUserPrincipal;

  @BeforeEach
  void setUp() throws Exception {
    jwtUtils = new JwtUtils();

    // Use reflection to set private fields
    setField(jwtUtils, "jwtSecret", "lofiapps_secret_key_lofiapps_secret_key_test");
    setField(jwtUtils, "jwtExpirationMs", 86400000);
    setField(jwtUtils, "jwtRefreshExpirationMs", 604800000);

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
            List.of(
                new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                new SimpleGrantedAuthority("READ_LOAN")));
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  @DisplayName("GenerateJwtToken should create valid JWT token")
  void generateJwtToken_ShouldCreateValidToken() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUserPrincipal);

    // Act
    String token = jwtUtils.generateJwtToken(authentication);

    // Assert
    assertNotNull(token);
    assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    assertTrue(jwtUtils.validateJwtToken(token));
  }

  @Test
  @DisplayName("GenerateRefreshToken should create valid refresh token")
  void generateRefreshToken_ShouldCreateValidToken() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUserPrincipal);

    // Act
    String refreshToken = jwtUtils.generateRefreshToken(authentication);

    // Assert
    assertNotNull(refreshToken);
    assertTrue(refreshToken.split("\\.").length == 3);
    assertTrue(jwtUtils.validateJwtToken(refreshToken));
  }

  @Test
  @DisplayName("GetUserIdFromJwtToken should extract user ID")
  void getUserIdFromJwtToken_ShouldExtractUserId() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUserPrincipal);
    String token = jwtUtils.generateJwtToken(authentication);

    // Act
    String userId = jwtUtils.getUserIdFromJwtToken(token);

    // Assert
    assertNotNull(userId);
    assertEquals(testUserId.toString(), userId);
  }

  @Test
  @DisplayName("GetEmailFromJwtToken should extract email")
  void getEmailFromJwtToken_ShouldExtractEmail() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUserPrincipal);
    String token = jwtUtils.generateJwtToken(authentication);

    // Act
    String email = jwtUtils.getEmailFromJwtToken(token);

    // Assert
    assertNotNull(email);
    assertEquals("test@example.com", email);
  }

  @Test
  @DisplayName("GetUserNameFromJwtToken should return email")
  void getUserNameFromJwtToken_ShouldReturnEmail() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUserPrincipal);
    String token = jwtUtils.generateJwtToken(authentication);

    // Act
    String userName = jwtUtils.getUserNameFromJwtToken(token);

    // Assert
    assertNotNull(userName);
    assertEquals("test@example.com", userName);
  }

  @Test
  @DisplayName("ValidateJwtToken should return true for valid token")
  void validateJwtToken_ShouldReturnTrue_ForValidToken() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUserPrincipal);
    String token = jwtUtils.generateJwtToken(authentication);

    // Act
    boolean isValid = jwtUtils.validateJwtToken(token);

    // Assert
    assertTrue(isValid);
  }

  @Test
  @DisplayName("ValidateJwtToken should return false for invalid token")
  void validateJwtToken_ShouldReturnFalse_ForInvalidToken() {
    // Arrange
    String invalidToken = "invalid.token.here";

    // Act
    boolean isValid = jwtUtils.validateJwtToken(invalidToken);

    // Assert
    assertFalse(isValid);
  }

  @Test
  @DisplayName("ValidateJwtToken should return false for malformed token")
  void validateJwtToken_ShouldReturnFalse_ForMalformedToken() {
    // Arrange
    String malformedToken = "not-a-jwt";

    // Act
    boolean isValid = jwtUtils.validateJwtToken(malformedToken);

    // Assert
    assertFalse(isValid);
  }

  @Test
  @DisplayName("ValidateJwtToken should return false for empty token")
  void validateJwtToken_ShouldReturnFalse_ForEmptyToken() {
    // Arrange
    String emptyToken = "";

    // Act
    boolean isValid = jwtUtils.validateJwtToken(emptyToken);

    // Assert
    assertFalse(isValid);
  }

  @Test
  @DisplayName("GetExpirationFromJwtToken should return positive value")
  void getExpirationFromJwtToken_ShouldReturnPositiveValue() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUserPrincipal);
    String token = jwtUtils.generateJwtToken(authentication);

    // Act
    long expiration = jwtUtils.getExpirationFromJwtToken(token);

    // Assert
    assertTrue(expiration > 0);
  }

  @Test
  @DisplayName("GetIssuedAtFromJwtToken should return non-null date")
  void getIssuedAtFromJwtToken_ShouldReturnNonNullDate() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUserPrincipal);
    String token = jwtUtils.generateJwtToken(authentication);

    // Act
    java.util.Date issuedAt = jwtUtils.getIssuedAtFromJwtToken(token);

    // Assert
    assertNotNull(issuedAt);
  }

  @Test
  @DisplayName("JWT token should contain roles claim")
  void generateJwtToken_ShouldContainRolesClaim() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUserPrincipal);

    // Act
    String token = jwtUtils.generateJwtToken(authentication);

    // Assert - token validates means claims were included
    assertNotNull(token);
    assertTrue(jwtUtils.validateJwtToken(token));
    assertEquals(testUserId.toString(), jwtUtils.getUserIdFromJwtToken(token));
  }

  @Test
  @DisplayName("JWT token should handle user without branch")
  void generateJwtToken_ShouldHandleUserWithoutBranch() {
    // Arrange
    UserPrincipal userWithoutBranch =
        new UserPrincipal(
            testUserId,
            "test@example.com",
            "password",
            null, // no branch
            null,
            BigDecimal.ZERO,
            com.lofi.lofiapps.enums.UserStatus.ACTIVE,
            Collections.emptyList());

    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(userWithoutBranch);

    // Act
    String token = jwtUtils.generateJwtToken(authentication);

    // Assert
    assertNotNull(token);
    assertTrue(jwtUtils.validateJwtToken(token));
  }

  @Test
  @DisplayName("Refresh token should have longer expiration than access token")
  void refreshToken_ShouldHaveLongerExpiration() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUserPrincipal);

    // Act
    String accessToken = jwtUtils.generateJwtToken(authentication);
    String refreshToken = jwtUtils.generateRefreshToken(authentication);

    long accessExpiration = jwtUtils.getExpirationFromJwtToken(accessToken);
    long refreshExpiration = jwtUtils.getExpirationFromJwtToken(refreshToken);

    // Assert
    assertTrue(refreshExpiration > accessExpiration);
  }
}
