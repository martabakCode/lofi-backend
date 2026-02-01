package com.lofi.lofiapps.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.request.CreateUserRequest;
import com.lofi.lofiapps.dto.request.UpdateProfileRequest;
import com.lofi.lofiapps.dto.request.UserCriteria;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.dto.response.UserProfileResponse;
import com.lofi.lofiapps.dto.response.UserSummaryResponse;
import com.lofi.lofiapps.enums.UserStatus;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.impl.usecase.user.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock private CreateUserUseCase createUserUseCase;

  @Mock private DeleteUserUseCase deleteUserUseCase;

  @Mock private GetUserProfileUseCase getUserProfileUseCase;

  @Mock private GetUsersUseCase getUsersUseCase;

  @Mock private UpdateProfileUseCase updateProfileUseCase;

  @Mock private GetProfilePhotoUseCase getProfilePhotoUseCase;

  @Mock private UpdateProfilePictureUseCase updateProfilePictureUseCase;

  @InjectMocks private UserServiceImpl userService;

  private UUID testUserId;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
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
            UserStatus.ACTIVE,
            Collections.emptyList());

    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userPrincipal);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @DisplayName("CreateUser should delegate to CreateUserUseCase")
  void createUser_ShouldDelegateToUseCase() {
    // Arrange
    CreateUserRequest request =
        CreateUserRequest.builder().email("newuser@example.com").fullName("New User").build();

    UserSummaryResponse expectedResponse =
        UserSummaryResponse.builder()
            .id(UUID.randomUUID())
            .email("newuser@example.com")
            .fullName("New User")
            .roles(Set.of("ROLE_CUSTOMER"))
            .status(UserStatus.ACTIVE)
            .build();

    when(createUserUseCase.execute(any(CreateUserRequest.class))).thenReturn(expectedResponse);

    // Act
    UserSummaryResponse result = userService.createUser(request);

    // Assert
    assertNotNull(result);
    assertEquals("newuser@example.com", result.getEmail());
    verify(createUserUseCase, times(1)).execute(request);
  }

  @Test
  @DisplayName("DeleteUser should delegate to DeleteUserUseCase")
  void deleteUser_ShouldDelegateToUseCase() {
    // Arrange
    doNothing().when(deleteUserUseCase).execute(testUserId);

    // Act
    userService.deleteUser(testUserId);

    // Assert
    verify(deleteUserUseCase, times(1)).execute(testUserId);
  }

  @Test
  @DisplayName("GetUserProfile should delegate to GetUserProfileUseCase")
  void getUserProfile_ShouldDelegateToUseCase() {
    // Arrange
    UserProfileResponse expectedResponse =
        UserProfileResponse.builder()
            .id(testUserId)
            .email("test@example.com")
            .fullName("Test User")
            .build();

    when(getUserProfileUseCase.execute(testUserId)).thenReturn(expectedResponse);

    // Act
    UserProfileResponse result = userService.getUserProfile(testUserId);

    // Assert
    assertNotNull(result);
    assertEquals(testUserId, result.getId());
    assertEquals("test@example.com", result.getEmail());
    verify(getUserProfileUseCase, times(1)).execute(testUserId);
  }

  @Test
  @DisplayName("GetMyProfile should get current user profile from security context")
  void getMyProfile_ShouldGetCurrentUserProfile() {
    // Arrange
    setupSecurityContext(testUserId);

    UserProfileResponse expectedResponse =
        UserProfileResponse.builder()
            .id(testUserId)
            .email("test@example.com")
            .fullName("Test User")
            .build();

    when(getUserProfileUseCase.execute(testUserId)).thenReturn(expectedResponse);

    // Act
    UserProfileResponse result = userService.getMyProfile();

    // Assert
    assertNotNull(result);
    assertEquals(testUserId, result.getId());
    verify(getUserProfileUseCase, times(1)).execute(testUserId);
  }

  @Test
  @DisplayName("GetMyProfile should throw exception when not authenticated")
  void getMyProfile_ShouldThrowException_WhenNotAuthenticated() {
    // Arrange - setup with non-UserPrincipal
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn("anonymousUser");
    SecurityContextHolder.setContext(securityContext);

    // Act & Assert
    assertThrows(RuntimeException.class, () -> userService.getMyProfile());
  }

  @Test
  @DisplayName("GetUsers should delegate to GetUsersUseCase")
  void getUsers_ShouldDelegateToUseCase() {
    // Arrange
    UserCriteria criteria = new UserCriteria();
    Pageable pageable = PageRequest.of(0, 10);

    UserSummaryResponse user =
        UserSummaryResponse.builder()
            .id(testUserId)
            .email("test@example.com")
            .fullName("Test User")
            .roles(Set.of("ROLE_CUSTOMER"))
            .status(UserStatus.ACTIVE)
            .build();

    PagedResponse<UserSummaryResponse> expectedResponse =
        PagedResponse.of(List.of(user), 0, 10, 1, 1);

    when(getUsersUseCase.execute(any(UserCriteria.class), any(Pageable.class)))
        .thenReturn(expectedResponse);

    // Act
    PagedResponse<UserSummaryResponse> result = userService.getUsers(criteria, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getMeta().getTotalItems());
    assertEquals(1, result.getItems().size());
    verify(getUsersUseCase, times(1)).execute(criteria, pageable);
  }

  @Test
  @DisplayName("UpdateProfile should delegate to UpdateProfileUseCase")
  void updateProfile_ShouldDelegateToUseCase() {
    // Arrange
    setupSecurityContext(testUserId);

    UpdateProfileRequest request = new UpdateProfileRequest();
    request.setFullName("Updated Name");
    request.setPhoneNumber("081234567890");
    String userAgent = "TestAgent";

    UserProfileResponse expectedResponse =
        UserProfileResponse.builder().id(testUserId).fullName("Updated Name").build();

    when(updateProfileUseCase.execute(eq(testUserId), any(UpdateProfileRequest.class), anyString()))
        .thenReturn(expectedResponse);

    // Act
    UserProfileResponse result = userService.updateProfile(request, userAgent);

    // Assert
    assertNotNull(result);
    assertEquals("Updated Name", result.getFullName());
    verify(updateProfileUseCase, times(1)).execute(eq(testUserId), eq(request), eq(userAgent));
  }

  @Test
  @DisplayName("UpdateProfilePicture should delegate to UpdateProfilePictureUseCase")
  void updateProfilePicture_ShouldDelegateToUseCase() {
    // Arrange
    setupSecurityContext(testUserId);

    MultipartFile photo =
        new MockMultipartFile("photo", "test.jpg", "image/jpeg", "test".getBytes());

    UserProfileResponse expectedResponse =
        UserProfileResponse.builder()
            .id(testUserId)
            .profilePictureUrl("https://example.com/photo.jpg")
            .build();

    when(updateProfilePictureUseCase.execute(eq(testUserId), any(MultipartFile.class)))
        .thenReturn(expectedResponse);

    // Act
    UserProfileResponse result = userService.updateProfilePicture(photo);

    // Assert
    assertNotNull(result);
    assertEquals("https://example.com/photo.jpg", result.getProfilePictureUrl());
    verify(updateProfilePictureUseCase, times(1)).execute(eq(testUserId), eq(photo));
  }

  @Test
  @DisplayName("GetProfilePhoto should delegate to GetProfilePhotoUseCase")
  void getProfilePhoto_ShouldDelegateToUseCase() {
    // Arrange
    byte[] expectedPhoto = "fake-image-data".getBytes();
    when(getProfilePhotoUseCase.execute(testUserId)).thenReturn(expectedPhoto);

    // Act
    byte[] result = userService.getProfilePhoto(testUserId);

    // Assert
    assertNotNull(result);
    assertArrayEquals(expectedPhoto, result);
    verify(getProfilePhotoUseCase, times(1)).execute(testUserId);
  }

  @Test
  @DisplayName("AnalyzeEligibility should return null (not implemented)")
  void analyzeEligibility_ShouldReturnNull() {
    // Act
    var result = userService.analyzeEligibility(testUserId);

    // Assert
    assertNull(result);
  }
}
