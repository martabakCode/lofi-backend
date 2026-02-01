package com.lofi.lofiapps.service.impl.usecase.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lofi.lofiapps.dto.request.GoogleLoginRequest;
import com.lofi.lofiapps.dto.response.LoginResponse;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.RoleName;
import com.lofi.lofiapps.enums.UserStatus;
import com.lofi.lofiapps.repository.BranchRepository;
import com.lofi.lofiapps.repository.RoleRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.security.jwt.JwtUtils;
import com.lofi.lofiapps.security.service.GoogleAuthService;
import com.lofi.lofiapps.security.service.GoogleUser;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class GoogleLoginUseCaseTest {

  @Mock private GoogleAuthService googleAuthService;
  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private BranchRepository branchRepository;
  @Mock private JwtUtils jwtUtils;

  @InjectMocks private GoogleLoginUseCase googleLoginUseCase;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void execute_InvalidToken_ShouldThrowException() {
    GoogleLoginRequest request = new GoogleLoginRequest();
    request.setIdToken("invalidToken");

    when(googleAuthService.verifyGoogleToken("invalidToken")).thenReturn(null);

    assertThrows(IllegalArgumentException.class, () -> googleLoginUseCase.execute(request));
  }

  @Test
  void execute_ValidToken_NewUser_ShouldRegisterAndLogin() {
    GoogleLoginRequest request = new GoogleLoginRequest();
    request.setIdToken("validToken");

    GoogleUser googleUser =
        GoogleUser.builder().email("new@example.com").name("New User").uid("uid123").build();

    when(googleAuthService.verifyGoogleToken("validToken")).thenReturn(googleUser);
    when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

    Role customerRole = Role.builder().name(RoleName.ROLE_CUSTOMER).build();
    when(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));

    User savedUser =
        User.builder()
            .id(UUID.randomUUID())
            .email("new@example.com")
            .status(UserStatus.ACTIVE)
            .roles(Collections.singleton(customerRole))
            .build();
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    when(jwtUtils.generateJwtToken(any())).thenReturn("jwtToken");
    when(jwtUtils.getExpirationFromJwtToken("jwtToken")).thenReturn(3600000L);

    LoginResponse response = googleLoginUseCase.execute(request);

    assertNotNull(response);
    assertEquals("jwtToken", response.getAccessToken());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void execute_ValidToken_ExistingUser_ShouldLogin() {
    GoogleLoginRequest request = new GoogleLoginRequest();
    request.setIdToken("validToken");

    GoogleUser googleUser =
        GoogleUser.builder().email("existing@example.com").uid("uid123").build();

    User existingUser =
        User.builder()
            .id(UUID.randomUUID())
            .email("existing@example.com")
            .status(UserStatus.ACTIVE)
            .roles(Collections.emptySet())
            .build();

    when(googleAuthService.verifyGoogleToken("validToken")).thenReturn(googleUser);
    when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(User.class))).thenReturn(existingUser); // For firebaseUid update

    when(jwtUtils.generateJwtToken(any())).thenReturn("jwtToken");

    LoginResponse response = googleLoginUseCase.execute(request);

    assertNotNull(response);
    verify(userRepository).save(existingUser); // Should be called to update firebaseUid
  }

  @Test
  void execute_BlockedUser_ShouldThrowException() {
    GoogleLoginRequest request = new GoogleLoginRequest();
    request.setIdToken("validToken");

    GoogleUser googleUser = GoogleUser.builder().email("blocked@example.com").build();
    User blockedUser =
        User.builder().email("blocked@example.com").status(UserStatus.BLOCKED).build();

    when(googleAuthService.verifyGoogleToken("validToken")).thenReturn(googleUser);
    when(userRepository.findByEmail("blocked@example.com")).thenReturn(Optional.of(blockedUser));

    assertThrows(RuntimeException.class, () -> googleLoginUseCase.execute(request));
  }
}
