package com.lofi.lofiapps.service.impl.auth;

import com.lofi.lofiapps.model.dto.request.LoginRequest;
import com.lofi.lofiapps.model.dto.response.LoginResponse;
import com.lofi.lofiapps.security.jwt.JwtUtils;
import com.lofi.lofiapps.security.service.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final com.lofi.lofiapps.repository.RefreshTokenRepository refreshTokenRepository;

  public LoginResponse execute(LoginRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);
    long expiration = jwtUtils.getExpirationFromJwtToken(jwt);

    String refreshTokenStr = jwtUtils.generateRefreshToken(authentication);
    long refreshExpiration = jwtUtils.getExpirationFromJwtToken(refreshTokenStr);
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    // Revoke existing tokens
    refreshTokenRepository.deleteByUserId(userPrincipal.getId());

    com.lofi.lofiapps.model.entity.RefreshToken refreshToken =
        com.lofi.lofiapps.model.entity.RefreshToken.builder()
            .user(com.lofi.lofiapps.model.entity.User.builder().id(userPrincipal.getId()).build())
            .token(refreshTokenStr)
            .expiryDate(java.time.Instant.now().plusMillis(refreshExpiration))
            .revoked(false)
            .build();

    refreshTokenRepository.save(refreshToken);

    return LoginResponse.builder()
        .accessToken(jwt)
        .refreshToken(refreshTokenStr)
        .expiresIn(expiration / 1000) // Seconds
        .tokenType("Bearer")
        .build();
  }
}
