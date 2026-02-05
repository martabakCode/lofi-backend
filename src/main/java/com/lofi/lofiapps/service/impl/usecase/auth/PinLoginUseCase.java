package com.lofi.lofiapps.service.impl.usecase.auth;

import com.lofi.lofiapps.dto.request.PinLoginRequest;
import com.lofi.lofiapps.dto.response.LoginResponse;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.security.jwt.JwtUtils;
import com.lofi.lofiapps.security.service.UserPrincipal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PinLoginUseCase {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;

  private static final int MAX_FAILED_ATTEMPTS = 5;
  private static final long LOCKOUT_DURATION_MINUTES = 30;

  @Transactional
  public LoginResponse execute(PinLoginRequest request) {
    User user =
        userRepository
            .findByUsername(request.getUsername())
            .or(() -> userRepository.findByEmail(request.getUsername()))
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Check account lockout
    if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS
        && user.getLastFailedLoginTime() != null
        && user.getLastFailedLoginTime()
            .isAfter(LocalDateTime.now().minusMinutes(LOCKOUT_DURATION_MINUTES))) {
      throw new RuntimeException(
          "Account locked. Please try again in " + LOCKOUT_DURATION_MINUTES + " minutes.");
    }

    // Verify PIN
    if (user.getPin() == null || !passwordEncoder.matches(request.getPin(), user.getPin())) {
      // Increment failed attempts
      user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
      user.setLastFailedLoginTime(LocalDateTime.now());
      userRepository.save(user);
      throw new RuntimeException("Invalid PIN");
    }

    // Reset failed attempts on successful login
    user.setFailedLoginAttempts(0);
    user.setLastFailedLoginTime(null);
    userRepository.save(user);

    // Generate JWT token
    UserPrincipal userPrincipal = UserPrincipal.create(user);
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    String token = jwtUtils.generateJwtToken(authentication);
    long expiration = jwtUtils.getExpirationFromJwtToken(token);

    return LoginResponse.builder()
        .accessToken(token)
        .expiresIn(expiration / 1000)
        .tokenType("Bearer")
        .pinSet(user.getPinSet())
        .profileCompleted(user.getProfileCompleted())
        .build();
  }
}
