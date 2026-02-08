package com.lofi.lofiapps.service.impl.usecase.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.request.SetGooglePinRequest;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class SetGooglePinUseCaseTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private SetGooglePinUseCase setGooglePinUseCase;

  @Test
  void execute_ShouldSetPin_WhenUserIsGoogleUserWithoutPin() {
    // Arrange
    UUID userId = UUID.randomUUID();
    SetGooglePinRequest request = new SetGooglePinRequest("123456");
    User user =
        User.builder().id(userId).firebaseUid("some-uid").password(null).pinSet(false).build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(request.getPin())).thenReturn("encodedPin");

    // Act
    setGooglePinUseCase.execute(userId, request);

    // Assert
    verify(userRepository).save(user);
    assertTrue(user.getPinSet());
    assertEquals("encodedPin", user.getPin());
  }

  @Test
  void execute_ShouldThrowException_WhenUserNotFound() {
    UUID userId = UUID.randomUUID();
    SetGooglePinRequest request = new SetGooglePinRequest("123456");
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> setGooglePinUseCase.execute(userId, request));
  }

  @Test
  void execute_ShouldThrowException_WhenUserHasPassword() {
    UUID userId = UUID.randomUUID();
    SetGooglePinRequest request = new SetGooglePinRequest("123456");
    User user = User.builder().password("password").build();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    Exception exception =
        assertThrows(RuntimeException.class, () -> setGooglePinUseCase.execute(userId, request));
    assertEquals("Use SetPinRequest for non-Google users", exception.getMessage());
  }

  @Test
  void execute_ShouldThrowException_WhenUserHasNoFirebaseUid() {
    UUID userId = UUID.randomUUID();
    SetGooglePinRequest request = new SetGooglePinRequest("123456");
    User user = User.builder().password(null).firebaseUid(null).build();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    Exception exception =
        assertThrows(RuntimeException.class, () -> setGooglePinUseCase.execute(userId, request));
    assertEquals("This endpoint is only for Google authenticated users", exception.getMessage());
  }

  @Test
  void execute_ShouldThrowException_WhenPinAlreadySet() {
    UUID userId = UUID.randomUUID();
    SetGooglePinRequest request = new SetGooglePinRequest("123456");
    User user = User.builder().password(null).firebaseUid("uid").pinSet(true).build();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    Exception exception =
        assertThrows(RuntimeException.class, () -> setGooglePinUseCase.execute(userId, request));
    assertEquals("PIN is already set. Use update PIN endpoint", exception.getMessage());
  }
}
