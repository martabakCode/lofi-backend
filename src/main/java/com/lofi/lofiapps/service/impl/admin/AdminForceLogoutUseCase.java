package com.lofi.lofiapps.service.impl.admin;

import com.lofi.lofiapps.security.service.TokenBlacklistService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminForceLogoutUseCase {
  private final TokenBlacklistService tokenBlacklistService;

  public void execute(UUID userId) {
    // This will mark the user for logout across all sessions
    tokenBlacklistService.forceLogoutUser(userId);
  }
}
