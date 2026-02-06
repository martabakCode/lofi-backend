package com.lofi.lofiapps.service.impl.usecase.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.request.LoginRequest;
import com.lofi.lofiapps.dto.request.RegisterRequest;
import com.lofi.lofiapps.dto.response.LoginResponse;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.RoleName;
import com.lofi.lofiapps.enums.UserStatus;
import com.lofi.lofiapps.repository.RoleRepository;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private LoginUseCase loginUseCase;

  @InjectMocks private RegisterUseCase registerUseCase;

  private RegisterRequest validRequest;

  @BeforeEach
  void setUp() {
    validRequest = new RegisterRequest();
    validRequest.setUsername("testuser");
    validRequest.setEmail("test@example.com");
    validRequest.setPassword("password123");
    validRequest.setFullName("Test User");
    validRequest.setPhoneNumber("+6281234567890");
  }

  @Test
  @DisplayName("Execute should register new user successfully")
  void execute_ShouldRegisterNewUserSuccessfully() {
    // Arrange
    when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);

    Role customerRole =
        Role.builder().id(java.util.UUID.randomUUID()).name(RoleName.ROLE_CUSTOMER).build();
    when(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));

    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    LoginResponse expectedResponse =
        LoginResponse.builder()
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .expiresIn(3600)
            .tokenType("Bearer")
            .build();
    when(loginUseCase.execute(any(LoginRequest.class))).thenReturn(expectedResponse);

    // Act
    LoginResponse result = registerUseCase.execute(validRequest);

    // Assert
    assertNotNull(result);
    assertEquals("access-token", result.getAccessToken());
    assertEquals("Bearer", result.getTokenType());

    verify(userRepository).existsByUsername(validRequest.getUsername());
    verify(userRepository).existsByEmail(validRequest.getEmail());
    verify(userRepository).save(any(User.class));
    verify(loginUseCase).execute(any(LoginRequest.class));
  }

  @Test
  @DisplayName("Execute should throw exception when username already exists")
  void execute_ShouldThrowException_WhenUsernameExists() {
    // Arrange
    when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(true);

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> registerUseCase.execute(validRequest));
    assertEquals("Username is already taken", exception.getMessage());

    verify(userRepository).existsByUsername(validRequest.getUsername());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Execute should throw exception when email already exists")
  void execute_ShouldThrowException_WhenEmailExists() {
    // Arrange
    when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> registerUseCase.execute(validRequest));
    assertEquals("Email is already in use", exception.getMessage());

    verify(userRepository).existsByUsername(validRequest.getUsername());
    verify(userRepository).existsByEmail(validRequest.getEmail());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Execute should create customer role if not exists")
  void execute_ShouldCreateCustomerRoleIfNotExists() {
    // Arrange
    when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
    when(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.empty());

    Role newRole =
        Role.builder().name(RoleName.ROLE_CUSTOMER).description("Default customer role").build();
    when(roleRepository.save(any(Role.class))).thenReturn(newRole);

    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    LoginResponse expectedResponse =
        LoginResponse.builder()
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .expiresIn(3600)
            .tokenType("Bearer")
            .build();
    when(loginUseCase.execute(any(LoginRequest.class))).thenReturn(expectedResponse);

    // Act
    LoginResponse result = registerUseCase.execute(validRequest);

    // Assert
    assertNotNull(result);
    verify(roleRepository).findByName(RoleName.ROLE_CUSTOMER);
    verify(roleRepository).save(any(Role.class));
  }

  @Test
  @DisplayName("Execute should encode password before saving")
  void execute_ShouldEncodePasswordBeforeSaving() {
    // Arrange
    when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);

    Role customerRole =
        Role.builder().id(java.util.UUID.randomUUID()).name(RoleName.ROLE_CUSTOMER).build();
    when(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));

    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    LoginResponse expectedResponse = LoginResponse.builder().accessToken("token").build();
    when(loginUseCase.execute(any(LoginRequest.class))).thenReturn(expectedResponse);

    // Act
    registerUseCase.execute(validRequest);

    // Assert
    verify(passwordEncoder).encode(validRequest.getPassword());
  }

  @Test
  @DisplayName("Execute should set user status to ACTIVE")
  void execute_ShouldSetUserStatusToActive() {
    // Arrange
    when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);

    Role customerRole =
        Role.builder().id(java.util.UUID.randomUUID()).name(RoleName.ROLE_CUSTOMER).build();
    when(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));

    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

    User savedUser =
        User.builder()
            .username(validRequest.getUsername())
            .email(validRequest.getEmail())
            .status(UserStatus.ACTIVE)
            .build();
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    LoginResponse expectedResponse = LoginResponse.builder().accessToken("token").build();
    when(loginUseCase.execute(any(LoginRequest.class))).thenReturn(expectedResponse);

    // Act
    registerUseCase.execute(validRequest);

    // Assert
    verify(userRepository).save(argThat(user -> user.getStatus() == UserStatus.ACTIVE));
  }
}
