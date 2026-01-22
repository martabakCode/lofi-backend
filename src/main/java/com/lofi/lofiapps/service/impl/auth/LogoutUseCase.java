package com.lofi.lofiapps.service.impl.auth;

import com.lofi.lofiapps.security.jwt.JwtUtils;
import com.lofi.lofiapps.security.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutUseCase {
  private final TokenBlacklistService tokenBlacklistService;
  private final JwtUtils jwtUtils;

  public void execute(String token) {
    if (token != null && jwtUtils.validateJwtToken(token)) {
      long expiration = jwtUtils.getExpirationFromJwtToken(token);
      tokenBlacklistService.blacklistToken(token, expiration);
    }
  }
}
