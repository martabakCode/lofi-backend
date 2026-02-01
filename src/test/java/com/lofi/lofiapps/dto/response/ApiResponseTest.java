package com.lofi.lofiapps.dto.response;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

  @Test
  @DisplayName("Success with data and message should create proper response")
  void success_WithDataAndMessage_ShouldCreateProperResponse() {
    // Arrange
    String testData = "Test Data";
    String message = "Operation successful";

    // Act
    ApiResponse<String> response = ApiResponse.success(testData, message);

    // Assert
    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertEquals(message, response.getMessage());
    assertEquals(testData, response.getData());
    assertNull(response.getCode());
    assertNull(response.getErrors());
  }

  @Test
  @DisplayName("Success with data only should use default message")
  void success_WithDataOnly_ShouldUseDefaultMessage() {
    // Arrange
    String testData = "Test Data";

    // Act
    ApiResponse<String> response = ApiResponse.success(testData);

    // Assert
    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertEquals("Request successful", response.getMessage());
    assertEquals(testData, response.getData());
  }

  @Test
  @DisplayName("Success with null data should work")
  void success_WithNullData_ShouldWork() {
    // Act
    ApiResponse<Object> response = ApiResponse.success(null, "No data");

    // Assert
    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertNull(response.getData());
  }

  @Test
  @DisplayName("Success with complex object should work")
  void success_WithComplexObject_ShouldWork() {
    // Arrange
    LoginResponse loginData =
        LoginResponse.builder()
            .accessToken("token")
            .refreshToken("refresh")
            .expiresIn(3600)
            .tokenType("Bearer")
            .build();

    // Act
    ApiResponse<LoginResponse> response = ApiResponse.success(loginData, "Login successful");

    // Assert
    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertNotNull(response.getData());
    assertEquals("token", response.getData().getAccessToken());
  }

  @Test
  @DisplayName("Error with code and message should create error response")
  void error_WithCodeAndMessage_ShouldCreateErrorResponse() {
    // Arrange
    String code = "AUTH_001";
    String message = "Authentication failed";

    // Act
    ApiResponse<Object> response = ApiResponse.error(code, message);

    // Assert
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertEquals(code, response.getCode());
    assertEquals(message, response.getMessage());
    assertNull(response.getData());
    assertNull(response.getErrors());
  }

  @Test
  @DisplayName("Error with code, message and errors should include all")
  void error_WithCodeMessageAndErrors_ShouldIncludeAll() {
    // Arrange
    String code = "VALIDATION_ERROR";
    String message = "Validation failed";
    Object errors = new String[] {"Field X is required", "Field Y must be positive"};

    // Act
    ApiResponse<Object> response = ApiResponse.error(code, message, errors);

    // Assert
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertEquals(code, response.getCode());
    assertEquals(message, response.getMessage());
    assertNull(response.getData());
    assertNotNull(response.getErrors());
  }

  @Test
  @DisplayName("Builder should work correctly")
  void builder_ShouldWorkCorrectly() {
    // Act
    ApiResponse<String> response =
        ApiResponse.<String>builder()
            .success(true)
            .message("Built response")
            .code("CODE_123")
            .data("Builder data")
            .errors("No errors")
            .build();

    // Assert
    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertEquals("Built response", response.getMessage());
    assertEquals("CODE_123", response.getCode());
    assertEquals("Builder data", response.getData());
    assertEquals("No errors", response.getErrors());
  }

  @Test
  @DisplayName("NoArgsConstructor should create empty response")
  void noArgsConstructor_ShouldCreateEmptyResponse() {
    // Act
    ApiResponse<String> response = new ApiResponse<>();

    // Assert
    assertNotNull(response);
    assertFalse(response.isSuccess()); // default false
    assertNull(response.getMessage());
    assertNull(response.getData());
  }

  @Test
  @DisplayName("AllArgsConstructor should set all fields")
  void allArgsConstructor_ShouldSetAllFields() {
    // Act
    ApiResponse<String> response =
        new ApiResponse<>(true, "All args message", "ALL_CODE", "All args data", "All args errors");

    // Assert
    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertEquals("All args message", response.getMessage());
    assertEquals("ALL_CODE", response.getCode());
    assertEquals("All args data", response.getData());
    assertEquals("All args errors", response.getErrors());
  }

  @Test
  @DisplayName("Setters and getters should work")
  void settersAndGetters_ShouldWork() {
    // Arrange
    ApiResponse<Integer> response = new ApiResponse<>();

    // Act
    response.setSuccess(true);
    response.setMessage("Setter test");
    response.setCode("SET_CODE");
    response.setData(42);
    response.setErrors("Setter errors");

    // Assert
    assertTrue(response.isSuccess());
    assertEquals("Setter test", response.getMessage());
    assertEquals("SET_CODE", response.getCode());
    assertEquals(42, response.getData());
    assertEquals("Setter errors", response.getErrors());
  }

  @Test
  @DisplayName("Generic type should preserve type information with PagedResponse")
  void genericType_ShouldPreserveTypeInformation() {
    // Arrange
    UserSummaryResponse user =
        UserSummaryResponse.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .fullName("Test User")
            .roles(Set.of("ROLE_CUSTOMER"))
            .build();

    PagedResponse<UserSummaryResponse> pagedData = PagedResponse.of(List.of(user), 0, 10, 100, 10);

    // Act
    ApiResponse<PagedResponse<UserSummaryResponse>> response =
        ApiResponse.success(pagedData, "Paged data retrieved");

    // Assert
    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertNotNull(response.getData());
    assertEquals(100, response.getData().getMeta().getTotalItems());
    assertEquals(10, response.getData().getMeta().getTotalPages());
  }
}
