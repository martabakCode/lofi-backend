package com.lofi.lofiapps.service.impl.auth;

import com.lofi.lofiapps.dto.response.LoginResponse;
import com.lofi.lofiapps.entity.RefreshToken;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.repository.RefreshTokenRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.security.jwt.JwtUtils;
import com.lofi.lofiapps.security.service.UserPrincipal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {
  private final JwtUtils jwtUtils;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;

  @Transactional
  public LoginResponse execute(String requestToken) {
    if (requestToken == null || !jwtUtils.validateJwtToken(requestToken)) {
      throw new IllegalArgumentException("Invalid Refresh Token");
    }

    RefreshToken token =
        refreshTokenRepository
            .findByToken(requestToken)
            .orElseThrow(() -> new IllegalArgumentException("Refresh Token not found in DB"));

    if (token.isRevoked()) {
      throw new IllegalArgumentException("Refresh Token Revoked (AUTH_REFRESH_TOKEN_REVOKED)");
    }

    if (token.getExpiryDate().isBefore(Instant.now())) {
      throw new IllegalArgumentException("Refresh Token Expired (AUTH_REFRESH_TOKEN_EXPIRED)");
    }

    // Refresh valid. Generate new Access Token.
    // Fetch fresh user to ensure roles are up to date
    User user =
        userRepository
            .findById(token.getUser().getId())
            .orElseThrow(() -> new IllegalStateException("User not found"));

    UserPrincipal principal = UserPrincipal.create(user);
    Authentication auth =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

    String newAccess = jwtUtils.generateJwtToken(auth);
    long expiration = jwtUtils.getExpirationFromJwtToken(newAccess);

    // We can allow the same refresh token to be used until it expires
    // Or we can rotate it here. For simplicity and "existing mechanism" style, just
    // return new Access Token.
    // If we want to return the same refresh token:

    return LoginResponse.builder()
        .accessToken(newAccess)
        .refreshToken(requestToken)
        .expiresIn(expiration / 1000)
        .tokenType("Bearer")
        .build();
  }
}
