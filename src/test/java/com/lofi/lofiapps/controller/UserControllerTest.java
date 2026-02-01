package com.lofi.lofiapps.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lofi.lofiapps.dto.request.CreateUserRequest;
import com.lofi.lofiapps.dto.request.UpdateProfileRequest;
import com.lofi.lofiapps.dto.response.UserProfileResponse;
import com.lofi.lofiapps.dto.response.UserSummaryResponse;
import com.lofi.lofiapps.enums.UserStatus;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.impl.AdminServiceImpl;
import com.lofi.lofiapps.service.impl.UserServiceImpl;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserControllerTest {

  private MockMvc mockMvc;

  @Mock private AdminServiceImpl adminService;

  @Mock private UserServiceImpl userService;

  @InjectMocks private UserController userController;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    objectMapper = new ObjectMapper();
  }

  private void setupSecurityContext(UUID userId) {
    UserPrincipal userPrincipal =
        new UserPrincipal(
            userId,
            "test@example.com",
            "password",
            UUID.randomUUID(),
            "Test Branch",
            BigDecimal.valueOf(10000000),
            com.lofi.lofiapps.enums.UserStatus.ACTIVE,
            Collections.emptyList());

    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userPrincipal);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @DisplayName("Create user should return created user")
  void createUser_ShouldReturnCreatedUser() throws Exception {
    // Arrange
    CreateUserRequest request =
        CreateUserRequest.builder().email("newuser@example.com").fullName("New User").build();

    UserSummaryResponse response =
        UserSummaryResponse.builder()
            .id(UUID.randomUUID())
            .email("newuser@example.com")
            .fullName("New User")
            .roles(Set.of("ROLE_CUSTOMER"))
            .status(UserStatus.ACTIVE)
            .build();

    when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

    // Act & Assert
    mockMvc
        .perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").value("newuser@example.com"));

    verify(userService, times(1)).createUser(any(CreateUserRequest.class));
  }

  @Test
  @DisplayName("Get my profile should return user profile")
  void getProfile_ShouldReturnUserProfile() throws Exception {
    // Arrange
    UUID userId = UUID.randomUUID();
    setupSecurityContext(userId);

    UserProfileResponse response =
        UserProfileResponse.builder()
            .id(userId)
            .email("test@example.com")
            .fullName("Test User")
            .build();

    when(userService.getMyProfile()).thenReturn(response);

    // Act & Assert
    mockMvc
        .perform(get("/users/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").value("test@example.com"));

    verify(userService, times(1)).getMyProfile();

    // Cleanup
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Update profile should return updated profile")
  void updateProfile_ShouldReturnUpdatedProfile() throws Exception {
    // Arrange
    UUID userId = UUID.randomUUID();
    setupSecurityContext(userId);

    UpdateProfileRequest request = new UpdateProfileRequest();
    request.setFullName("Updated Name");
    request.setPhoneNumber("081234567890");

    UserProfileResponse response =
        UserProfileResponse.builder()
            .id(userId)
            .email("test@example.com")
            .fullName("Updated Name")
            .build();

    when(userService.updateProfile(any(UpdateProfileRequest.class), any())).thenReturn(response);

    // Act & Assert
    mockMvc
        .perform(
            put("/users/me")
                .header("User-Agent", "TestAgent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.fullName").value("Updated Name"));

    verify(userService, times(1)).updateProfile(any(UpdateProfileRequest.class), any());

    // Cleanup
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Update profile photo should return updated profile")
  void updateProfilePhoto_ShouldReturnUpdatedProfile() throws Exception {
    // Arrange
    UUID userId = UUID.randomUUID();
    setupSecurityContext(userId);

    MockMultipartFile photo =
        new MockMultipartFile("photo", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test".getBytes());

    UserProfileResponse response =
        UserProfileResponse.builder()
            .id(userId)
            .profilePictureUrl("https://example.com/photo.jpg")
            .build();

    when(userService.updateProfilePicture(any())).thenReturn(response);

    // Act & Assert
    mockMvc
        .perform(
            multipart("/users/me/photo")
                .file(photo)
                .with(
                    request -> {
                      request.setMethod("PUT");
                      return request;
                    }))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(userService, times(1)).updateProfilePicture(any());

    // Cleanup
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Get my profile photo should return photo bytes")
  void getMyProfilePhoto_ShouldReturnPhotoBytes() throws Exception {
    // Arrange
    UUID userId = UUID.randomUUID();
    setupSecurityContext(userId);

    byte[] photoBytes = "fake-image-data".getBytes();
    when(userService.getProfilePhoto(userId)).thenReturn(photoBytes);

    // Act & Assert
    mockMvc
        .perform(get("/users/me/photo"))
        .andExpect(status().isOk())
        .andExpect(content().bytes(photoBytes));

    verify(userService, times(1)).getProfilePhoto(userId);

    // Cleanup
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Force logout should return success")
  void forceLogout_ShouldReturnSuccess() throws Exception {
    // Arrange
    UUID userId = UUID.randomUUID();
    doNothing().when(adminService).forceLogoutUser(userId);

    // Act & Assert
    mockMvc
        .perform(post("/users/admin/users/{userId}/force-logout", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(adminService, times(1)).forceLogoutUser(userId);
  }

  @Test
  @DisplayName("Delete account should return success")
  void deleteAccount_ShouldReturnSuccess() throws Exception {
    // Arrange
    UUID userId = UUID.randomUUID();
    setupSecurityContext(userId);
    doNothing().when(userService).deleteUser(userId);

    // Act & Assert
    mockMvc
        .perform(delete("/users/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(userService, times(1)).deleteUser(userId);

    // Cleanup
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Admin delete user should return success")
  void deleteUser_ShouldReturnSuccess() throws Exception {
    // Arrange
    UUID userId = UUID.randomUUID();
    doNothing().when(userService).deleteUser(userId);

    // Act & Assert
    mockMvc
        .perform(delete("/users/admin/users/{userId}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(userService, times(1)).deleteUser(userId);
  }

  @Test
  @DisplayName("Get user profile photo by ID should return photo bytes")
  void getUserProfilePhoto_ShouldReturnPhotoBytes() throws Exception {
    // Arrange
    UUID userId = UUID.randomUUID();
    byte[] photoBytes = "fake-image-data".getBytes();
    when(userService.getProfilePhoto(userId)).thenReturn(photoBytes);

    // Act & Assert
    mockMvc
        .perform(get("/users/{userId}/photo", userId))
        .andExpect(status().isOk())
        .andExpect(content().bytes(photoBytes));

    verify(userService, times(1)).getProfilePhoto(userId);
  }
}
