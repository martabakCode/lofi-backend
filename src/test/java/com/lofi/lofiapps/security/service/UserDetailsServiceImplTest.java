package com.lofi.lofiapps.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserDetailsServiceImpl userDetailsService;

  @Test
  void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
    String email = "test@example.com";
    User user =
        User.builder()
            .id(UUID.randomUUID())
            .email(email)
            .password("password")
            .username("testuser")
            .roles(Collections.emptySet())
            .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    UserDetails result = userDetailsService.loadUserByUsername(email);

    assertNotNull(result);
    assertEquals(email, ((UserPrincipal) result).getEmail());
    verify(userRepository).findByEmail(email);
  }

  @Test
  void loadUserByUsername_WhenUserNotFound_ShouldThrowException() {
    String email = "nonexistent@example.com";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(
        UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(email));

    verify(userRepository).findByEmail(email);
  }
}
