package com.lofi.lofiapps.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RequestDtoValidationTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Nested
  @DisplayName("LoginRequest Validation Tests")
  class LoginRequestTests {

    @Test
    @DisplayName("Valid LoginRequest should have no violations")
    void validLoginRequest_ShouldHaveNoViolations() {
      // Arrange
      LoginRequest request = new LoginRequest();
      request.setEmail("test@example.com");
      request.setPassword("password123");

      // Act
      Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

      // Assert
      assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("LoginRequest with blank email should have violation")
    void blankEmail_ShouldHaveViolation() {
      // Arrange
      LoginRequest request = new LoginRequest();
      request.setEmail("");
      request.setPassword("password123");

      // Act
      Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("LoginRequest with invalid email should have violation")
    void invalidEmail_ShouldHaveViolation() {
      // Arrange
      LoginRequest request = new LoginRequest();
      request.setEmail("not-an-email");
      request.setPassword("password123");

      // Act
      Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("LoginRequest with blank password should have violation")
    void blankPassword_ShouldHaveViolation() {
      // Arrange
      LoginRequest request = new LoginRequest();
      request.setEmail("test@example.com");
      request.setPassword("");

      // Act
      Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
    }
  }

  @Nested
  @DisplayName("RegisterRequest Validation Tests")
  class RegisterRequestTests {

    @Test
    @DisplayName("Valid RegisterRequest should have no violations")
    void validRegisterRequest_ShouldHaveNoViolations() {
      // Arrange
      RegisterRequest request =
          RegisterRequest.builder()
              .fullName("John Doe")
              .username("johndoe")
              .email("john@example.com")
              .password("password123")
              .phoneNumber("+6281234567890")
              .build();

      // Act
      Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

      // Assert
      assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("RegisterRequest with blank fullName should have violation")
    void blankFullName_ShouldHaveViolation() {
      // Arrange
      RegisterRequest request =
          RegisterRequest.builder()
              .fullName("")
              .username("johndoe")
              .email("john@example.com")
              .password("password123")
              .phoneNumber("+6281234567890")
              .build();

      // Act
      Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
      assertTrue(
          violations.stream().anyMatch(v -> v.getMessage().contains("Full name is required")));
    }

    @Test
    @DisplayName("RegisterRequest with short password should have violation")
    void shortPassword_ShouldHaveViolation() {
      // Arrange
      RegisterRequest request =
          RegisterRequest.builder()
              .fullName("John Doe")
              .username("johndoe")
              .email("john@example.com")
              .password("12345") // less than 6 chars
              .phoneNumber("+6281234567890")
              .build();

      // Act
      Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
      assertTrue(
          violations.stream()
              .anyMatch(v -> v.getMessage().contains("Password must be at least 6 characters")));
    }

    @Test
    @DisplayName("RegisterRequest with invalid phone number should have violation")
    void invalidPhoneNumber_ShouldHaveViolation() {
      // Arrange
      RegisterRequest request =
          RegisterRequest.builder()
              .fullName("John Doe")
              .username("johndoe")
              .email("john@example.com")
              .password("password123")
              .phoneNumber("123") // too short
              .build();

      // Act
      Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
      assertTrue(
          violations.stream()
              .anyMatch(v -> v.getMessage().contains("Invalid phone number format")));
    }

    @Test
    @DisplayName("RegisterRequest with invalid email should have violation")
    void invalidEmail_ShouldHaveViolation() {
      // Arrange
      RegisterRequest request =
          RegisterRequest.builder()
              .fullName("John Doe")
              .username("johndoe")
              .email("invalid-email")
              .password("password123")
              .phoneNumber("+6281234567890")
              .build();

      // Act
      Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
      assertTrue(
          violations.stream().anyMatch(v -> v.getMessage().contains("Invalid email format")));
    }
  }

  @Nested
  @DisplayName("ForgotPasswordRequest Validation Tests")
  class ForgotPasswordRequestTests {

    @Test
    @DisplayName("Valid ForgotPasswordRequest should have no violations")
    void validRequest_ShouldHaveNoViolations() {
      // Arrange
      ForgotPasswordRequest request = new ForgotPasswordRequest();
      request.setEmail("test@example.com");

      // Act
      Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);

      // Assert
      assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("ForgotPasswordRequest with blank email should have violation")
    void blankEmail_ShouldHaveViolation() {
      // Arrange
      ForgotPasswordRequest request = new ForgotPasswordRequest();
      request.setEmail("");

      // Act
      Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
    }
  }

  @Nested
  @DisplayName("ResetPasswordRequest Validation Tests")
  class ResetPasswordRequestTests {

    @Test
    @DisplayName("Valid ResetPasswordRequest should have no violations")
    void validRequest_ShouldHaveNoViolations() {
      // Arrange
      ResetPasswordRequest request = new ResetPasswordRequest();
      request.setToken("valid-token");
      request.setNewPassword("newpassword123");

      // Act
      Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);

      // Assert
      assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("ResetPasswordRequest with blank token should have violation")
    void blankToken_ShouldHaveViolation() {
      // Arrange
      ResetPasswordRequest request = new ResetPasswordRequest();
      request.setToken("");
      request.setNewPassword("newpassword123");

      // Act
      Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("ResetPasswordRequest with short password should have violation")
    void shortPassword_ShouldHaveViolation() {
      // Arrange
      ResetPasswordRequest request = new ResetPasswordRequest();
      request.setToken("valid-token");
      request.setNewPassword("12345"); // less than 6 chars

      // Act
      Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
    }
  }

  @Nested
  @DisplayName("ChangePasswordRequest Validation Tests")
  class ChangePasswordRequestTests {

    @Test
    @DisplayName("Valid ChangePasswordRequest should have no violations")
    void validRequest_ShouldHaveNoViolations() {
      // Arrange
      ChangePasswordRequest request = new ChangePasswordRequest();
      request.setOldPassword("oldpassword123");
      request.setNewPassword("newpassword123");

      // Act
      Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

      // Assert
      assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("ChangePasswordRequest with blank oldPassword should have violation")
    void blankOldPassword_ShouldHaveViolation() {
      // Arrange
      ChangePasswordRequest request = new ChangePasswordRequest();
      request.setOldPassword("");
      request.setNewPassword("newpassword123");

      // Act
      Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("ChangePasswordRequest with short newPassword should have violation")
    void shortNewPassword_ShouldHaveViolation() {
      // Arrange
      ChangePasswordRequest request = new ChangePasswordRequest();
      request.setOldPassword("oldpassword123");
      request.setNewPassword("12345"); // less than 6 chars

      // Act
      Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
    }
  }

  @Nested
  @DisplayName("RefreshTokenRequest Validation Tests")
  class RefreshTokenRequestTests {

    @Test
    @DisplayName("Valid RefreshTokenRequest should have no violations")
    void validRequest_ShouldHaveNoViolations() {
      // Arrange
      RefreshTokenRequest request = new RefreshTokenRequest();
      request.setRefreshToken("valid-refresh-token");

      // Act
      Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);

      // Assert
      assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("RefreshTokenRequest with blank token should have violation")
    void blankToken_ShouldHaveViolation() {
      // Arrange
      RefreshTokenRequest request = new RefreshTokenRequest();
      request.setRefreshToken("");

      // Act
      Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
      assertTrue(
          violations.stream().anyMatch(v -> v.getMessage().contains("Refresh token is required")));
    }
  }

  @Nested
  @DisplayName("GoogleLoginRequest Validation Tests")
  class GoogleLoginRequestTests {

    @Test
    @DisplayName("Valid GoogleLoginRequest should have no violations")
    void validRequest_ShouldHaveNoViolations() {
      // Arrange
      GoogleLoginRequest request = new GoogleLoginRequest();
      request.setIdToken("google-id-token");

      // Act
      Set<ConstraintViolation<GoogleLoginRequest>> violations = validator.validate(request);

      // Assert
      assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("GoogleLoginRequest with blank idToken should have violation")
    void blankIdToken_ShouldHaveViolation() {
      // Arrange
      GoogleLoginRequest request = new GoogleLoginRequest();
      request.setIdToken("");

      // Act
      Set<ConstraintViolation<GoogleLoginRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("GoogleLoginRequest with location should be valid")
    void requestWithLocation_ShouldBeValid() {
      // Arrange
      GoogleLoginRequest request = new GoogleLoginRequest();
      request.setIdToken("google-id-token");
      request.setLatitude(-6.2088);
      request.setLongitude(106.8456);

      // Act
      Set<ConstraintViolation<GoogleLoginRequest>> violations = validator.validate(request);

      // Assert
      assertTrue(violations.isEmpty());
    }
  }

  @Nested
  @DisplayName("UpdateProfileRequest Validation Tests")
  class UpdateProfileRequestTests {

    @Test
    @DisplayName("Valid UpdateProfileRequest should have no violations")
    void validRequest_ShouldHaveNoViolations() {
      // Arrange
      UpdateProfileRequest request = new UpdateProfileRequest();
      request.setFullName("John Doe");
      request.setPhoneNumber("+6281234567890");

      // Act
      Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

      // Assert
      assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("UpdateProfileRequest with blank fullName should have violation")
    void blankFullName_ShouldHaveViolation() {
      // Arrange
      UpdateProfileRequest request = new UpdateProfileRequest();
      request.setFullName("");
      request.setPhoneNumber("+6281234567890");

      // Act
      Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("UpdateProfileRequest with blank phoneNumber should have violation")
    void blankPhoneNumber_ShouldHaveViolation() {
      // Arrange
      UpdateProfileRequest request = new UpdateProfileRequest();
      request.setFullName("John Doe");
      request.setPhoneNumber("");

      // Act
      Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

      // Assert
      assertFalse(violations.isEmpty());
    }
  }
}
